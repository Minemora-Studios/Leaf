package org.dreeam.leaf.config.modules.opt;

import org.dreeam.leaf.config.ConfigCategory;
import org.dreeam.leaf.config.ConfigModule;

/**
 * Minemora specific tuning knobs.
 * <p>
 * Every option here defaults to the exact vanilla / upstream value, so installing
 * the patches that read them changes nothing until a value is actually set.
 */
public class MinemoraTweaks extends ConfigModule {

    public String basePath() {
        return ConfigCategory.PERF.basePath() + ".minemora";
    }

    /** Paper's EAR uses a hardcoded 3 here. Pufferfish uses 20. */
    public static int inactiveGoalSelectorInterval = 3;
    /** Vanilla AcquirePoi.SCAN_RANGE. */
    public static int villagerHomeSearchRadius = 48;
    /** Vanilla AcquirePoi.SCAN_RANGE. */
    public static int villagerBreedingBedSearchRadius = 48;

    /** Goal selector ticks between two MoveThroughVillageGoal#canUse attempts. 0 or 1 disables the gate. */
    public static int moveThroughVillageInterval = 0;
    /** Ticks to wait after the first failed MoveThroughVillageGoal#canUse. 0 disables the back-off. */
    public static int moveThroughVillageRetryMin = 20;
    /** Upper bound of the exponential back-off. */
    public static int moveThroughVillageRetryMax = 200;

    @Override
    public void onLoaded() {
        inactiveGoalSelectorInterval = Math.max(1, globalConfig.getInt(basePath() + ".inactive-goal-selector-interval", inactiveGoalSelectorInterval,
            """
                How often, in ticks, the goal selector of an EAR-inactive mob is updated.
                Paper hardcodes 3, Pufferfish uses 20. Only affects mobs that entity
                activation range already decided not to tick fully, so higher values trade
                reaction time of far away mobs for tick time. 1 disables the throttle."""));

        villagerHomeSearchRadius = Math.max(1, globalConfig.getInt(basePath() + ".villager-home-search-radius", villagerHomeSearchRadius,
            """
                Radius in blocks used by SetClosestHomeAsWalkTarget when a villager walks
                back to its bed. Vanilla is 48 (AcquirePoi.SCAN_RANGE). This runs for every
                villager during the REST activity, so on servers with large villager
                populations it is one of the heaviest POI lookups of the night.
                A villager whose bed is further away than this will not walk back to it."""));

        villagerBreedingBedSearchRadius = Math.max(1, globalConfig.getInt(basePath() + ".villager-breeding-bed-search-radius", villagerBreedingBedSearchRadius,
            """
                Radius in blocks used by VillagerMakeLove to claim a vacant bed for the baby.
                Vanilla is 48 (AcquirePoi.SCAN_RANGE). Lowering this below the distance
                between your breeders and their spare beds will stop them from breeding."""));

        moveThroughVillageInterval = Math.max(0, globalConfig.getInt(basePath() + ".move-through-village-interval", moveThroughVillageInterval,
            """
                Goal selector ticks between two MoveThroughVillageGoal#canUse attempts.
                That check is one of the most expensive in vanilla: RandomPos#generateRandomPos
                runs ten iterations, each performing a radius 10 PoiManager#find, followed by up
                to three full A* passes. 0 or 1 keeps vanilla behaviour, 20 matches the scale of
                performance.entity-goal.start-tick-chance. Redundant with the back-off below for
                mobs that keep failing, so only raise it if the profiler still shows the goal."""));

        moveThroughVillageRetryMin = Math.max(0, globalConfig.getInt(basePath() + ".move-through-village-retry-min", moveThroughVillageRetryMin,
            """
                Ticks to wait before retrying MoveThroughVillageGoal#canUse after it fails once.
                A mob that cannot reach a village POI fails for a reason that does not change
                within a tick, so re-running the full search every tick is wasted work.
                The delay doubles on every consecutive failure up to the maximum below and
                resets as soon as the check succeeds. 0 disables the back-off entirely,
                which restores exact vanilla behaviour."""));

        moveThroughVillageRetryMax = Math.max(moveThroughVillageRetryMin, globalConfig.getInt(basePath() + ".move-through-village-retry-max", moveThroughVillageRetryMax,
            """
                Upper bound, in ticks, of the MoveThroughVillageGoal back-off. A permanently
                stuck mob settles here. Worst case latency for a mob that becomes able to reach
                a village again is this value."""));
    }

}
