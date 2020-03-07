package org.mbari.cthulhu.app;

import org.mbari.cthulhu.ui.player.PlayerComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static com.google.common.base.MoreObjects.toStringHelper;

/**
 * Component that manages the main media annotating media player components for the application.
 */
final class PlayerComponents {

    private static final Logger log = LoggerFactory.getLogger(PlayerComponents.class);

    /**
     * Map of current player components, keyed by their unique identifier.
     */
    private final Map<UUID, PlayerComponent> playerComponents = new HashMap<>();

    /**
     * Currently active (i.e. focussed) player component.
     */
    private PlayerComponent active;

    PlayerComponents() {
    }

    /**
     * Get a particular player component.
     *
     * @param uuid unique identifier of the desired player component
     * @return optional player component
     */
    Optional<PlayerComponent> get(UUID uuid) {
        return Optional.ofNullable(playerComponents.get(uuid));
    }

    /**
     * Open a player component.
     *
     * @return player component
     */
    PlayerComponent open() {
        log.debug("open()");
        return open(UUID.randomUUID());
    }

    /**
     * Open a player component.
     * <p>
     * If there is an existing player component with the specified uuid it will be returned, otherwise a new player
     * component will be created.
     *
     * @param uuid unique identifer for the player component
     * @return player component
     */
    PlayerComponent open(UUID uuid) {
        log.debug("open(uuid={})", uuid);
        PlayerComponent playerComponent = playerComponents.get(uuid);
        log.debug("playerComponent={}", playerComponent);
        if (playerComponent == null) {
            log.debug("uuid not found, creating new");
            playerComponent = new PlayerComponent(uuid);
            playerComponents.put(uuid, playerComponent);
        }
        log.debug("opened uuid: {}", uuid);
        return playerComponent;
    }

    /**
     * Close a player component.
     * <p>
     * If the last player component is closed, exit the application.
     *
     * @param uuid unique identifier of the player component to close
     * @return <code>true</code> if the player component was closed; <code>false</code> if it was not (e.g. does not exist)
     */
    boolean close(UUID uuid) {
        log.debug("close(uuid={})", uuid);
        PlayerComponent playerComponent = playerComponents.get(uuid);
        log.debug("playerComponent={}", playerComponent);
        if (playerComponent != null) {
            // Is the active player component being closed?
            if (playerComponent.equals(active)) {
                // No active component, a new active player component will be set on a subsequent stage focus event
                active(null);
            }
            playerComponent.close();
            playerComponents.remove(uuid);
            log.debug("closed uuid: {}", uuid);
            return true;
        } else {
            log.warn("unknown uuid: {}", uuid);
            return false;
        }
    }

    /**
     * Close all player components.
     */
    void closeAll() {
        log.debug("closeAll()");
        playerComponents.values().forEach(PlayerComponent::close);
        playerComponents.clear();
    }

    /**
     * Show a player component.
     *
     * @param uuid unique identifier of the player component to close
     * @return <code>true</code> if the player component was shown; <code>false</code> if it was not (e.g. does not exist)
     */
    boolean show(UUID uuid) {
        log.debug("show(uuid={})", uuid);
        PlayerComponent playerComponent = playerComponents.get(uuid);
        log.debug("playerComponent={}", playerComponent);
        if (playerComponent != null) {
            playerComponent.show();
            log.debug("showed uuid: {}", uuid);
            return true;
        } else {
            log.warn("unknown uuid: {}", uuid);
            return false;
        }
    }

    /**
     * Get all of the currently active player components.
     *
     * @return unmodifiable map of all of the currently active player components
     */
    Map<UUID, PlayerComponent> playerComponents() {
        return Collections.unmodifiableMap(playerComponents);
    }

    /**
     * Determine if there are any currently active player component.
     *
     * @return <code>true</code> if there is at least one active player component; <code>false</code> if not
     */
    boolean empty() {
        return playerComponents.isEmpty();
    }

    /**
     * Track the currently active (i.e. focussed) media player component.
     *
     * @param playerComponent newly active component, may be <code>null</code>
     */
    void active(PlayerComponent playerComponent) {
        log.debug("active(playerComponent={})", playerComponent);
        this.active = playerComponent;
    }

    /**
     * Get the active player component.
     *
     * @return the currently active player component
     */
    Optional<PlayerComponent> active() {
        return Optional.ofNullable(active);
    }

    @Override
    public String toString() {
        return toStringHelper(this)
            .add("playerComponents", playerComponents)
            .add("active", active)
            .toString();
    }
}
