# Proposed Swing-to-JavaFX migration

## Goal and boundaries

Migrate FreeRails' production desktop UI from Swing to JavaFX in five
incremental milestones. At each milestone, the project must build and the
current Swing application must remain runnable as an end-to-end fallback.
JavaFX becomes the default only after the migrated application supports the
existing launch modes and gameplay workflows.

The objective is to preserve the current layout, information hierarchy,
controls and behavior, not to redesign the interface. Include the launcher,
gameplay window, map and overview, menus, dialogs, reports, and server-only
screens. Keep game rules, network protocol, scenario/save-game formats, and
standalone tools or test utilities out of scope unless one blocks the
production migration.

Keep UI-framework dependencies in the client and startup layers. The game
model, move and server contracts should not acquire JavaFX dependencies.
Reuse controllers, services and rendering logic where their current
Swing-specific boundaries allow it.

## Current starting point

`freerails.Start` composes the client and optional local server, handles the
`--both`, `--client` and `--server` roles, and opens the Swing launcher on the
AWT event-dispatch thread. `LauncherFrame` hosts the setup wizard;
`ClientFrame` is the main gameplay window. `GUIComponentFactory` and many
classes under `freerails.client.view` and `freerails.client.renderer` expose
Swing or AWT types. `GameLoop` currently updates the game model and drives
custom Swing/Java2D painting. `LauncherServices` is a useful boundary between
the launcher and host operations.

The Gradle build targets JDK 21. JavaFX is supplied through platform-aware
Gradle runtime dependencies and included in the executable fat JAR. JavaFX
native runtime artifacts are platform-specific, so the fat JAR must be built
on each supported operating system to include the matching runtime.

## Migration strategy

Keep the Swing UI and JavaFX UI as separate implementations during the
transition. Add an explicit `--javafx` way to select the JavaFX UI while
retaining the existing command-line role selection and a fully supported
Swing path. Do not embed Swing controls in JavaFX scenes. During intermediate
milestones, the JavaFX launcher may hand off to the existing Swing gameplay
window until the gameplay migration is complete.

Each milestone should be independently runnable. The Swing path is the
end-to-end control case until cutover; the JavaFX path is exercised for every
surface already migrated. Automated tests should cover framework-independent
logic and the new UI boundaries where practical. Repeatable launch and
gameplay smoke checks are also needed, including a visual comparison against
the existing layout.

## Milestones

### 1. Add JavaFX runtime and a selectable application path

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

### 2. Migrate the launcher and setup flow

- Rebuild the launcher wizard, client/server options, scenario and save-game
  selection, and connected-player display with JavaFX controls.
- Reuse `LauncherServices` for hosting, loading, and connected-player updates;
  extract any remaining service contract from Swing-specific components.
- Keep the Swing launcher available as a fallback. Until the gameplay UI is
  migrated, the JavaFX launcher can hand off to the existing Swing game window.

**Runnable checks:** In both UI selections, exercise server-only startup, local
hosting, remote-client setup, scenario selection, save-game loading, and
connected-player updates. Confirm that options persist and errors remain
visible to the user.

### 3. Migrate the gameplay shell, map, and input

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

### 4. Migrate remaining gameplay surfaces and verify parity

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

- Make JavaFX the default UI after the JavaFX path passes the parity checks;
  retain an explicit Swing fallback for one final transition period if useful.
- Remove the obsolete Swing launcher/game UI, Swing-only event and repaint
  infrastructure, and unused dependencies. Retain Java2D only where it is
  still deliberately used for rendering or image processing.
- Update build, launch and developer documentation to describe the final
  JavaFX runtime and supported role arguments.

**Runnable checks:** Run the full test and check tasks, build the distributable,
and smoke-test all supported startup roles on each supported platform.
Confirm that the default launch is JavaFX, the final UI has no unintended
Swing dependencies, and layout and gameplay behavior remain consistent.

## Cross-cutting risks and acceptance criteria

- **Thread ownership:** Swing currently relies on the AWT event-dispatch
  thread. JavaFX scene and control updates must happen on the JavaFX
  application thread. Define how game-model updates and view notifications
  cross that boundary; avoid blocking the UI thread or unsafe cross-thread
  access to JavaFX nodes.
- **Rendering and responsiveness:** The current game loop uses custom
  Java2D/Swing painting. Preserve its update cadence and avoid copying large
  images every frame without measuring performance. Confirm map panning,
  animation, and input remain responsive.
- **Parallel-path drift:** During migration, changes to shared workflows can
  otherwise fix one UI while regressing the other. Keep shared business logic
  framework-independent and include the Swing fallback in milestone smoke
  checks until it is removed.
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

The migration is complete when the JavaFX path is the supported default, all
production application screens and launch roles work without Swing UI, the
final distributable includes its JavaFX runtime, and the existing automated
and manual checks show no unintended gameplay or layout regressions.
