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
 *
 * <p>
 * Lange Inhaltszeilen werden nach RFC 5545 Abschnitt 3.1 gefaltet: eine
 * physische Zeile ist hoechstens 75 Oktette lang, der Rest folgt auf
 * Folgezeilen, die mit einem einzelnen Leerzeichen beginnen. Ohne diese
 * Faltung erzeugte ein langer Modulname eine Zeile, die strenge Parser
 * ablehnen oder abschneiden.
 * </p>
 */
public final class IcsExporter {

    private static final DateTimeFormatter DATUM_FORMAT =
            DateTimeFormatter.BASIC_ISO_DATE; // yyyyMMdd

    /**
     * Maximale Laenge einer physischen Zeile in Oktetten (RFC 5545, 3.1).
     * Das Leerzeichen der Folgezeile zaehlt mit, daher bleiben fuer deren
     * Inhalt nur 74 Oktette.
     */
    private static final int MAX_ZEILENLAENGE = 75;

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
        appendZeile(sb, "BEGIN:VCALENDAR");
        appendZeile(sb, "VERSION:2.0");
        appendZeile(sb, "PRODID:-//Studentischer Organisationshelfer//Fristenexport//DE");
        appendZeile(sb, "CALSCALE:GREGORIAN");
        appendZeile(sb, "METHOD:PUBLISH");

        for (Deadline d : deadlines) {
            appendEvent(sb, d);
        }

        appendZeile(sb, "END:VCALENDAR");
        Files.write(zielPfad, sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    private static void appendEvent(StringBuilder sb, Deadline d) {
        String datum = DATUM_FORMAT.format(d.getDatum());
        String datumEnde = DATUM_FORMAT.format(d.getDatum().plusDays(1)); // DTEND exklusiv
        String uid = "frist-" + datum + "-" + inhaltsKennung(d)
                + "@studentischer-organisationshelfer";
        String stamp = LocalDateTime.now(ZoneOffset.UTC)
                .format(DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'"));

        appendZeile(sb, "BEGIN:VEVENT");
        appendZeile(sb, "UID:" + uid);
        appendZeile(sb, "DTSTAMP:" + stamp);
        appendZeile(sb, "DTSTART;VALUE=DATE:" + datum);
        appendZeile(sb, "DTEND;VALUE=DATE:" + datumEnde);
        // getTitel() returns the description (there is no separate title
        // field any more), so a DESCRIPTION line would only repeat SUMMARY
        // verbatim - and many calendar clients then show the text twice.
        appendZeile(sb, "SUMMARY:" + escape(d.getTitel()));
        if (d.getModulName() != null && !d.getModulName().isBlank()) {
            // Kategorien sind ein Komma-separiertes Feld - ein Wert ist sicher.
            appendZeile(sb, "CATEGORIES:" + escape(d.getModulName()));
        }
        appendZeile(sb, "TRANSP:TRANSPARENT");
        appendZeile(sb, "END:VEVENT");
    }

    /**
     * Haengt eine Inhaltszeile an - gefaltet, falls sie laenger als 75 Oktette
     * ist, und in jedem Fall mit CRLF abgeschlossen.
     */
    private static void appendZeile(StringBuilder sb, String zeile) {
        sb.append(falte(zeile));
    }

    /**
     * Faltet eine Inhaltszeile nach RFC 5545 Abschnitt 3.1.
     *
     * <p>
     * Gezaehlt werden Oktette (UTF-8), nicht Zeichen: die Grenze von 75 gilt
     * fuer die kodierte Zeile. Folgezeilen beginnen mit einem Leerzeichen, das
     * selbst mitzaehlt, und tragen daher nur 74 Oktette Inhalt. Der
     * Zeilenumbruch ist immer CRLF; ein blosses LF wuerde die Faltung
     * unlesbar machen.
     * </p>
     *
     * <p>
     * Ein Trennpunkt wird nie mitten in ein Mehrbyte-Zeichen gelegt - sonst
     * entstuende beim Entfalten ein Ersatzzeichen statt des Umlauts. Beim
     * Entfalten (CRLF + ein Leerzeichen entfernen) ergibt sich wieder exakt
     * die Ausgangszeile.
     * </p>
     *
     * @param zeile die logische Inhaltszeile ohne Zeilenende
     * @return die gefaltete Zeile inklusive abschliessendem CRLF
     */
    static String falte(String zeile) {
        byte[] oktette = zeile.getBytes(StandardCharsets.UTF_8);
        if (oktette.length <= MAX_ZEILENLAENGE) {
            return zeile + "\r\n";
        }

        StringBuilder sb = new StringBuilder(zeile.length() + 16);
        int pos = 0;
        boolean ersteZeile = true;
        while (pos < oktette.length) {
            int budget = ersteZeile ? MAX_ZEILENLAENGE : MAX_ZEILENLAENGE - 1;
            int ende = Math.min(pos + budget, oktette.length);
            // Nicht mitten in einem UTF-8-Zeichen trennen: 0b10xxxxxx ist
            // eine Fortsetzungsbyte, also zurueck bis zum Startzeichen.
            while (ende < oktette.length && ende > pos && (oktette[ende] & 0xC0) == 0x80) {
                ende--;
            }
            if (!ersteZeile) {
                sb.append(' ');
            }
            sb.append(new String(oktette, pos, ende - pos, StandardCharsets.UTF_8));
            sb.append("\r\n");
            pos = ende;
            ersteZeile = false;
        }
        return sb.toString();
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
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                .replace(";", "\\;")
                .replace(",", "\\,")
                .replace("\r\n", "\\n")
                .replace("\n", "\\n")
                .replace("\r", "\\n");
    }
}
