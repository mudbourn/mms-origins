package info.mudbourn.mmsorigins.entity;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import java.util.EnumSet;

/**
 * Keeps a henchman near its summoner, pathing to the owner once they wander too
 * far and stopping again once close. A trimmed cousin of vanilla's
 * {@code FollowOwnerGoal}, which only serves tamable animals.
 */
public class HenchmanFollowOwnerGoal extends Goal {

    private final FloranHenchman henchman;
    private final double speed;
    private final float startDistance;
    private final float stopDistance;
    private final PathNavigation navigation;
    private LivingEntity owner;

    public HenchmanFollowOwnerGoal(FloranHenchman henchman, double speed, float startDistance, float stopDistance) {
        this.henchman = henchman;
        this.speed = speed;
        this.startDistance = startDistance;
        this.stopDistance = stopDistance;
        this.navigation = henchman.getNavigation();
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity candidate = this.henchman.getOwner();
        if (candidate == null || candidate.isSpectator()) {
            return false;
        }
        if (this.henchman.distanceToSqr(candidate) < this.startDistance * this.startDistance) {
            return false;
        }
        this.owner = candidate;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return this.owner != null
            && !this.navigation.isDone()
            && this.henchman.distanceToSqr(this.owner) > this.stopDistance * this.stopDistance;
    }

    @Override
    public void stop() {
        this.owner = null;
        this.navigation.stop();
    }

    @Override
    public void tick() {
        if (this.owner == null) {
            return;
        }
        this.henchman.getLookControl().setLookAt(this.owner, 10.0f, this.henchman.getMaxHeadXRot());
        this.navigation.moveTo(this.owner, this.speed);
    }
}
