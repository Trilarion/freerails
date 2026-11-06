# Swing-to-JavaFX migration

## Goal and boundaries

FreeRails' production desktop UI has migrated from Swing to JavaFX in five
incremental milestones. JavaFX is the only production UI and supports all
existing launch modes and gameplay workflows.

The objective is to preserve the current layout, information hierarchy,
controls and behavior, not to redesign the interface. Include the launcher,
gameplay window, map and overview, menus, dialogs, reports, and server-only
screens. Keep game rules, network protocol, scenario/save-game formats, and
standalone tools or test utilities out of scope unless one blocks the
production migration.

Keep UI-framework dependencies in the client and startup layers. The game
model, move and server contracts do not depend on JavaFX. Reuse controllers,
services and rendering logic behind toolkit-independent boundaries.

## Completed migration

`freerails.Start` composes the JavaFX client and optional local server for
`--both`, `--client`, and `--server`; with no arguments it starts `--both`.
`JavaFxLauncher` provides scenario and save-game selection, client options,
connected players, and server-only setup. `LauncherServices` supports local
hosting and loading. `JavaFxLauncherClient` runs network and model updates away
from the JavaFX application thread, while `JavaFxMapView` adapts the existing
Java2D map renderers into JavaFX images.

The JavaFX gameplay surface includes the map, overview, train list, status
area, selection/scroll/build input, terrain and station details, track,
station, and industry building, train production and schedule editing,
save/load and exit flows, broker transactions, and financial reports. These
workflows use existing game moves and report generators. Swing launcher,
gameplay, event, and repaint infrastructure has been removed from production
code. AWT/Java2D remains only for shared rendering and image processing.

The Gradle build targets JDK 21. JavaFX is supplied through platform-aware
Gradle runtime dependencies and included in the executable fat JAR. The
controls and WebView modules are used by the JavaFX launcher and HTML reports.
JavaFX native runtime artifacts are platform-specific, so the fat JAR must be
built on each supported operating system to include the matching runtime.
Local-host mode listens on loopback; the standalone server role listens on all
network interfaces so remote clients can connect.

## Migration strategy used

The migration kept toolkit-specific presentation separate and preserved the
existing command-line role selection. JavaFX was exercised incrementally for
each migrated surface before the final cutover. Automated tests cover
framework-independent logic and JavaFX UI boundaries where practical; visual
comparisons against the former Swing layout were used to check parity.

## Milestones

The descriptions below record each milestone's scope and acceptance checks;
milestones 1-4 were completed before the final cutover.

### 1. Add JavaFX runtime and a selectable application path (complete)

- Add JavaFX dependencies and platform-aware runtime packaging to Gradle.
- Introduce JavaFX startup and shutdown lifecycle handling without changing
  the current Swing default or its role arguments.
- Add an explicit `--javafx` selection path and a minimal JavaFX launcher shell
  with a handoff to the current Swing launcher.
- Keep server composition and launcher services independent of either toolkit.

**Runnable checks:** Build and run the existing Swing application in all
supported roles. Start the JavaFX shell, close it cleanly, and verify that
`--javafx` composes with `--both`, `--client` and `--server` without changing
their role semantics. Verify the platform-resolved fat JAR on each supported
platform.

### 2. Migrate the launcher and setup flow (complete)

- Rebuild the launcher wizard, client/server options, scenario and save-game
  selection, and connected-player display with JavaFX controls.
- Reuse `LauncherServices` for hosting, loading, and connected-player updates;
  extract any remaining service contract from Swing-specific components.
- Keep the Swing launcher available as a separate fallback during gameplay
  migration.

**Runnable checks:** In both UI selections, exercise server-only startup, local
hosting, remote-client setup, scenario selection, save-game loading, and
connected-player updates. Confirm that options persist and errors remain
visible to the user.

### 3. Migrate the gameplay shell, map, and input (complete)

- Recreate the main gameplay window's existing arrangement: map and overview,
  train tabs, status area, and the existing menu placement.
- Move map and overview rendering behind JavaFX-compatible view boundaries.
  Reuse the current Java2D renderers through an image adapter initially if
  that preserves behavior; port layers to JavaFX drawing only where needed.
- Route map mouse and keyboard input to the existing controllers, preserving
  selection, scrolling, build actions and other gameplay behavior.
- Keep game-model updates off the JavaFX application thread. Apply UI changes
  on that thread and preserve the existing update cadence and smooth rendering.

**Runnable checks:** Start a game through the JavaFX path and verify map and
overview rendering, resizing, scrolling, input, and representative build and
train actions. Confirm that the game remains responsive while simulation
updates run. Continue to run the complete Swing path.

### 4. Migrate remaining gameplay surfaces and verify parity (complete)

- Rebuild the remaining panels, tab contents, menus, popups, dialogs and
  reports in JavaFX, including save/load and exit flows.
- Replace Swing-specific component factories and listener/update wiring at the
  client boundary; preserve existing controller and service behavior.
- Complete JavaFX coverage for both locally hosted and remote-client roles,
  including the server-only UI.

**Runnable checks:** Exercise the complete game workflow in JavaFX: start or
join a game, build and manage trains, open reports and dialogs, save and load,
and exit. Compare the migrated screens with the Swing layout at the same
window size and verify that the Swing fallback still works.

### 5. Switch the default and remove Swing UI

- **Complete.** JavaFX is the only production UI; the redundant `--javafx`
  option and Swing fallback have been removed. The `--both`, `--client`, and
  `--server` role arguments remain supported.
- Swing launcher/gameplay UI, Swing-only event and repaint infrastructure,
  and obsolete UI helpers have been removed. Java2D is retained for rendering
  and image processing used by JavaFX.
- Build and developer documentation describes the JavaFX runtime and supported
  role arguments.

**Runnable checks:** Run the full test and check tasks, build the distributable,
and smoke-test all supported startup roles on each supported platform. Confirm
that default launch is JavaFX, production UI code has no Swing dependencies,
and layout and gameplay behavior remain consistent.

## Cross-cutting risks and acceptance criteria

- **Thread ownership:** JavaFX scene and control updates must happen on the
  JavaFX application thread. Game-model updates and view notifications cross
  that boundary without blocking the UI thread or accessing JavaFX nodes from
  other threads.
- **Rendering and responsiveness:** The JavaFX map view adapts existing Java2D
  renderers. Preserve the update cadence and avoid copying large images every
  frame without measuring performance. Keep map panning, animation, and input
  responsive.
- **Toolkit boundaries:** Keep shared business logic framework-independent;
  JavaFX views should use existing controllers and services rather than adding
  UI-framework dependencies to shared layers.
- **Runtime distribution:** JavaFX libraries include platform-specific native
  components and are not supplied by the project's current JDK target alone.
  Validate Gradle resolution and launch packaging for every supported
  platform, not just compilation on the development machine.
- **Visual parity:** Compare screenshots at consistent window sizes and check
  layout proportions, text, menus, map scaling, status display, and dialog
  behavior. Treat visual redesign and new UX as follow-up work.
- **Behavioral parity:** Preserve existing launch roles, game controls,
  networking, save/load behavior, error reporting, and user options. Changes
  to game rules or persisted/networked data are not part of this migration.

The migration is complete: JavaFX is the only production UI, all production
application screens and launch roles work without Swing UI, the final
distributable includes its JavaFX runtime, and automated and manual checks
show no unintended gameplay or layout regressions.
