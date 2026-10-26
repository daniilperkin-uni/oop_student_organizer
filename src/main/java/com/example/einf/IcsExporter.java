package com.example.einf;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.List;

/**
 * Exportiert Fristen als iCalendar-Datei (.ics), die sich in jeden
 * gaengigen Kalender (Google, Outlook, Apple) importieren laesst.
 *
 * <p>
 * Die Datei wird als VEVENT mit ganztägigem Termin (DTEND exklusiv, wie im
 * RFC 5545 gefordert) je Deadline erzeugt. Zeilenumbrueche sind CRLF, alle
 * Zeilen enden mit CRLF - der RFC verlangt das und manche Parser sind
 * kleinlich.
 * </p>
 */
public final class IcsExporter {

    private static final DateTimeFormatter DATUM_FORMAT =
            DateTimeFormatter.BASIC_ISO_DATE; // yyyyMMdd

    private IcsExporter() {
    }

    /**
     * Schreibt alle Fristen als Ganztagestermine in eine .ics-Datei.
     *
     * @param deadlines die zu exportierenden Fristen
     * @param zielPfad  Zieldatei (typischerweise ending auf .ics)
     * @throws IOException beim Schreiben der Datei
     */
    public static void exportiere(List<Deadline> deadlines, Path zielPfad) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("BEGIN:VCALENDAR\r\n");
        sb.append("VERSION:2.0\r\n");
        sb.append("PRODID:-//Studentischer Organisationshelfer//Fristenexport//DE\r\n");
        sb.append("CALSCALE:GREGORIAN\r\n");
        sb.append("METHOD:PUBLISH\r\n");

        for (Deadline d : deadlines) {
            appendEvent(sb, d);
        }

        sb.append("END:VCALENDAR\r\n");
        Files.write(zielPfad, sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    private static void appendEvent(StringBuilder sb, Deadline d) {
        String datum = DATUM_FORMAT.format(d.getDatum());
        String datumEnde = DATUM_FORMAT.format(d.getDatum().plusDays(1)); // DTEND exklusiv
        String uid = "frist-" + datum + "-" + inhaltsKennung(d)
                + "@studentischer-organisationshelfer";
        String stamp = LocalDateTime.now(ZoneOffset.UTC)
                .format(DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'"));

        sb.append("BEGIN:VEVENT\r\n");
        sb.append("UID:").append(uid).append("\r\n");
        sb.append("DTSTAMP:").append(stamp).append("\r\n");
        sb.append("DTSTART;VALUE=DATE:").append(datum).append("\r\n");
        sb.append("DTEND;VALUE=DATE:").append(datumEnde).append("\r\n");
        // getTitel() returns the description (there is no separate title
        // field any more), so a DESCRIPTION line would only repeat SUMMARY
        // verbatim - and many calendar clients then show the text twice.
        sb.append("SUMMARY:").append(escape(d.getTitel())).append("\r\n");
        if (d.getModulName() != null && !d.getModulName().isBlank()) {
            // Kategorien sind ein Komma-separiertes Feld - ein Wert ist sicher.
            sb.append("CATEGORIES:").append(escape(d.getModulName())).append("\r\n");
        }
        sb.append("TRANSP:TRANSPARENT\r\n");
        sb.append("END:VEVENT\r\n");
    }

    /**
     * Stabile Kennung einer Frist fuer die UID.
     *
     * <p>
     * Frueher stand hier {@code Integer.toHexString(d.hashCode())} - und weil
     * {@link Deadline} weder {@code equals} noch {@code hashCode} definiert,
     * war das ein Identitaets-Hash: jeder Export erzeugte eine neue UID, und
     * ein Re-Import der aktualisierten Datei legte die Termine doppelt an,
     * statt sie zu aktualisieren.
     * </p>
     *
     * <p>
     * Die Kennung entsteht jetzt aus dem Inhalt (Titel, Datum, Modulname) und
     * bleibt damit fuer denselben Termin bei jedem Export gleich. Der
     * SHA-256-Hash wird auf 16 Hex-Zeichen gekuerzt; das reicht bei Weitem,
     * um Kollisionen zwischen den Fristen eines Nutzers auszuschliessen.
     * </p>
     */
    static String inhaltsKennung(Deadline d) {
        // Unit Separator als Trenner: kann in keinem der drei Felder vorkommen,
        // sonst koennten verschiedene Kombinationen denselben Text ergeben.
        String basis = d.getTitel() + "\u001F" + d.getDatum() + "\u001F"
                + (d.getModulName() == null ? "" : d.getModulName());
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(basis.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash, 0, 8);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 muss in jeder JVM vorhanden sein.
            throw new IllegalStateException("SHA-256 ist nicht verfuegbar", e);
        }
    }

    /**
     * Escaping nach RFC 5545 Abschnitt 3.3.11: Backslash, Semikolon, Komma
     * und Zeilenumbrueche innerhalb von Textwerten.
     *
     * <p>
     * {@code \r\n} wird zuerst behandelt, danach ein einzelnes {@code \n} und
     * schliesslich ein einzelnes {@code \r}. Ein alleinstehendes CR (z. B. aus
     * eingefuegtem Text) wuerde in einem ICS-Textwert sonst als
     * Zeilentrenner missverstanden.
     * </p>
     */
    static String escape(String text) {
        return text.replace("\\", "\\\\")
                .replace(";", "\\;")
                .replace(",", "\\,")
                .replace("\r\n", "\\n")
                .replace("\n", "\\n")
                .replace("\r", "\\n");
    }
}
