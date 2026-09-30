# Repository Notes (oop_student_organizer)

JavaFX 21 desktop app (archived university course project). The portfolio-wide
policy for all repositories in "Uni old Projects" lives in `../AGENTS.md` -
read that first; this file only adds repository-specific facts.

## Build, run, test

- CI runs `./mvnw -B verify` on Temurin 21 and is the ground truth. Run it (or
  the local fallback below) before claiming any change works.
- The built jar deliberately has no `Main-Class`: the GUI only starts via
  `javafx:run`; `java -jar` does not work.
- On this Windows machine `./mvnw` cannot download its wrapper distribution
  (CI is fine, network-dependent). Run the cached Maven with JDK 21 instead:

  ```bash
  MVN_HOME="C:/Users/danyo/.m2/wrapper/dists/apache-maven-3.8.5-bin/5i5jha092a3i37g0paqnfr15e0/apache-maven-3.8.5"
  "C:/Program Files/Java/jdk-21/bin/java" -classpath "$MVN_HOME/boot/plexus-classworlds-2.6.0.jar" \
    -Dclassworlds.conf="$MVN_HOME/bin/m2.conf" -Dmaven.home="$MVN_HOME" \
    -Dmaven.multiModuleProjectDirectory="<repo dir, C:/-style path>" \
    org.codehaus.plexus.classworlds.launcher.Launcher -B verify
  ```

  Swap the last goal for `-B test`, `-B compile` or `javafx:run`.

## Data and compatibility rules

- Real user data lives OUTSIDE the repo at `%USERPROFILE%\.studenthelfer`
  (`module.csv`, `deadlines.csv`, `.bak` backups, semicolon-separated). Never
  print, move, delete or rewrite it; tests must keep using `@TempDir`.
- The custom CSV escaping in `SpeicherManager` and the legacy four-column
  branch that reads the `ModulStatus` constants are deliberate format
  compatibility - do not "clean up" without a data migration.
- German class names, UI strings, comments and umlauts are intentional;
  preserve them (UTF-8).

## Conventions

- Commits: plain English imperative subject plus a detailed body (no
  conventional-commit prefixes); commit only when explicitly asked.
- `HomeController` deliberately keeps all `@FXML` injections for the single
  `home-view.fxml`; do not restructure into `fx:include` part files - there is
  no FXML-loading test coverage.
- Read-only by default: explain and navigate freely, change code only on
  explicit request.
