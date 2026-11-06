## Added

- Generated 26.3 recipes, models, tags, advancements, and spellbook chapters under `src/main/generated` so the repository can build the same JAR from a fresh checkout.
- Registered the mimic chest data serializer through Fabric's entity data registry.

## Changed

- Updated the mod and build dependencies for Minecraft 26.3 and Java 25.
- Migrated entity, block entity, item, HUD, spell, accessory, and Mine Little Pony rendering to Minecraft's current extract and submit APIs.
- Ported persistence hooks to `ValueInput` and `ValueOutput`, and updated entity, gravity, fishing, spawn, tooltip, and data fix mixins to current method signatures.
- Moved Unicopia's custom block breaking progress into the 26.3 client level render data path.
- Updated data generation for item component binding, spellbook text and item serialization, and child advancement backgrounds.
- Updated custom recipe serializers to keep item stack templates until 26.3 binds item components. Spellbook recipe previews now build when requested.
- Updated structure processor and spawn override data to 26.3 formats, and supplied the wooden rod item tag used by Unicopia recipes.
- Gave custom game rules namespaced, lowercase identifiers required by 26.3.
- Removed stale access widener targets and excluded the old mapping only data importer utilities from test compilation.

## Fixed

- Fixed death respawning becoming stuck with a disabled Respawn button: race-specific spawn adjustment now retains vanilla's valid spawn when no preferred location is found, rather than returning null to the teleport transition. Corrected the inverted ground/cloud check that rejected normal ground for Earth Ponies. Live client tests completed death/request/respawn cycles for Earth Pony, Pegasus, Bat Pony, and Seapony with keep-inventory enabled and disabled.

- Corrected Pegasus flight camera banking to use the FOV effect strength rather than the FOV angle. Apply the roll to the camera orientation so its view, basis vectors, and frustum agree, and banking preserves the viewing direction.
- Apply exertion and energy FOV additions to the final angle instead of the vanilla multiplier, preventing extreme flight zoom distortion. A live Pegasus test passed 898 checks for heading stability, disabling roll with the FOV effects slider, and a valid exertion FOV.
- Skip animal posing for render states without living-entity caster data, fixing a minecart rendering crash discovered during the flight test.

- Fixed a rendering crash in wind particles by supplying the complete 26.3 entity vertex format. The shared correction also covers rainbow trails and restores rainboom ring texture coordinates. Passive and attached wind, rainbow trails, and rainboom rings survived a live client world test.

- Apply rearing transforms before queuing the body and accessories so separate wearables share the body's transform. Verified the Stetson on an Earth Pony during hang, arms-up, stomp, and climb, including matching body/accessory matrices and head poses.
- Keep child hat, sleeve, and pants layers in their local poses so head and limb movement is inherited once. This prevents detached hats and doubled clothing movement during hanging and rearing animations.
- Verified child-layer alignment during hang and arms-up poses on an Earth Pony model in the client.
- Reapply player and animal joint poses after the deferred model renderer resets animations. This fixes pony bodies remaining still while hats animate during emotes and abilities.
- Verified kick and two-hoof wave poses on a Mine Little Pony model in an isolated client; the final distributable build passes.
- Corrected the right foreleg in the Wololo animation and safely handle nose animations on models without pony muzzle parts. Emote names and recipient types now accept lowercase as well as the existing uppercase spelling.
- The full Gradle build now succeeds and produces a JAR.
- Data generation completes. A forced class load audit of 85 reachable mixin targets also completes without a transformation error.
- The client loads Unicopia, creates a window, and starts the sound engine without a fatal Unicopia resource error.
- Placed fruit, shell, and palm sign models now load their sprites from the block atlas. Spawn eggs and the pale oak basket have valid item sprites.
- Client precipitation again stops above Unicopia's cloud layer. Raging sky tint and camera roll use the 26.3 render state. Celestial body rotation now targets the current sky render call.
- Server entity tracker packets use the 26.3 synchronizer, and guardian targeting uses the current callback signature.
- The dedicated server creates a fresh 26.3 world, loads Unicopia recipes and advancements, prepares spawn, and reaches the ready state.
- Cloud islands stay within the world generation chunk radius. Empty entity references no longer trigger data decode errors when chunks load.
- A saved server world reloads, and server commands can place a cloud block, locate a cloud village, and summon a butterfly without log errors.
- Restored the portal destination camera pass and projected portal surface shader. A paired portal visibly displays destination terrain and sky in a live 26.3 client world.
- Restored corruption desaturation with a 26.3 post effect and live saturation uniform updates. The effect loaded and processed frames in a client test.
- Restored optional Farmer's Delight cutting recipes and both apple pie conversion recipes, including their recipe unlocks, using 26.3 formats.
- The full Gradle build passes with Farmer's Delight 26.3 enabled, and a dedicated server loads the restored recipes and reaches the ready state without recipe errors.
- Fixed player join serialization for an empty hanging position, race ID decoding, and updates sent before a player connection exists. Corrected projectile and HUD heart mixins found during the live world test.
- Prevented humanoid bracelet layers from rendering on non-humanoid mobs. A normal client world remained loaded for over a minute after this fix.
- Restored placed-spell visibility, spell rune tint and opacity, and the reversed depth test for custom magic surfaces. Preserved the portal opening animation and original 120 degree destination camera field of view.
- Fixed optional placed-spell and portal state decoding, empty spell records, and restored portal registry entries during world reload.
- Portal transport and levitating items changing dimensions now use absolute destinations while preserving motion, preventing the destination from being added to the entity's current position.
- A creative player entered one test portal and arrived at its linked portal about 12 blocks away at the expected height. The client then saved the world and exited normally.
- Replaced the velocity HUD's per-pixel arc submissions with smooth arc meshes, preventing a severe rendering stall while flying. Cloud particles now use the cloud outline shape and handle empty shapes safely.

## Known limitations

- A paired portal visibly renders destination terrain and sky in a client test. Nested portals reuse the previous frame; the former recursion depth setting has not been verified against the old renderer.
- The desaturation post effect has been exercised with a forced test value. Its appearance at real corruption levels still needs in-game review.
- Some migrated visual details, including model attachments and HUD layout, still need in game review. The optional HDSkins dependency logs an `asset_id` data error during client resource loading.
- Client world loading, paired portal rendering and transport, and a clean world save are verified. Longer play sessions and broader visual parity still need review.
