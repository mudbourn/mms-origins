package info.mudbourn.mmsorigins.entity;

import info.mudbourn.mmsorigins.MmsOrigins;
import info.mudbourn.mmsorigins.power.condition.SunIntakeCondition;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.VariableIntPower;
import java.util.EnumSet;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.ai.control.JumpControl;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GolemRandomStrollInVillageGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsTargetGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.golem.AbstractGolem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * A floran's summoned Grovekeeper: a jungle-grown creaking that serves its
 * summoner the way an iron golem serves a village.
 *
 * <p>It runs the iron golem's goals (hunting nearby monsters, avenging blows,
 * strolling, and now and then holding out a flower to its summoner or a
 * villager, which it drops at their feet), but walks, sounds and animates like
 * a creaking. It locks in place while a player it is hostile to holds their
 * crosshair on it, the enderman's stare test turned around. Every blow it
 * lands saps the victim with slowness. Its owner is stored by uuid so it can
 * follow them and is barred from ever targeting them.
 */
public class FloranHenchman extends AbstractGolem {

    private static final EntityDataAccessor<Boolean> CAN_MOVE =
        SynchedEntityData.defineId(FloranHenchman.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<ItemStack> OFFERED_FLOWER =
        SynchedEntityData.defineId(FloranHenchman.class, EntityDataSerializers.ITEM_STACK);
    private static final byte ATTACK_EVENT = 4;
    public static final float ATTACK_ANIMATION_SPEED = 0.6f;
    private static final int ATTACK_ANIMATION_TICKS = 25;
    private static final int SLOWNESS_TICKS = 100;
    private static final int LIFESPAN_TICKS = 2400;
    private static final int TAUNT_INTERVAL = 20;
    private static final double TAUNT_RADIUS = 16.0;
    private static final double LEASH_RANGE = 32.0;
    private static final double FREEZE_RANGE = 24.0;
    private static final double CROSSHAIR_TOLERANCE = 0.025;
    private static final Identifier CHARGE_ID =
        Identifier.fromNamespaceAndPath(MmsOrigins.MOD_ID, "sun_power");
    public static final int CHARGE_COST = 30;

    public final AnimationState attackAnimationState = new AnimationState();
    private int attackAnimationRemainingTicks;
    private UUID ownerUuid;
    private int age;
    private boolean refundOnRemoval = true;

    private static final Identifier MAELS_BLESSING_ID = Identifier.fromNamespaceAndPath(MmsOrigins.MOD_ID, "maels_blessing");

    private static final double MAELS_BLESSING_BONUS = 0.5;

    private static final Identifier SUN_SPEED_ID = Identifier.fromNamespaceAndPath(MmsOrigins.MOD_ID, "sun_speed");

    private static final double SUN_SPEED_PEAK = 0.3;

    public FloranHenchman(EntityType<? extends AbstractGolem> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
        this.lookControl = new FrozenAwareLookControl(this);
        this.moveControl = new FrozenAwareMoveControl(this);
        this.jumpControl = new FrozenAwareJumpControl(this);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 40.0)
            .add(Attributes.ATTACK_DAMAGE, 3.75)
            .add(Attributes.MOVEMENT_SPEED, 0.25)
            .add(Attributes.FOLLOW_RANGE, 32.0)
            .add(Attributes.STEP_HEIGHT, 1.0625);
    }

    /** Strengthens a henchman raised under Mael's Blessing. */
    public void empower() {
        this.getAttribute(Attributes.MAX_HEALTH).addPermanentModifier(new AttributeModifier(
            MAELS_BLESSING_ID, MAELS_BLESSING_BONUS, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        this.getAttribute(Attributes.ATTACK_DAMAGE).addPermanentModifier(new AttributeModifier(
            MAELS_BLESSING_ID, MAELS_BLESSING_BONUS, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        this.setHealth(this.getMaxHealth());
    }

    /** Quickens the henchman along the sun curve while it stands under open sky. */
    private void updateSunSpeed() {
        AttributeInstance speed = this.getAttribute(Attributes.MOVEMENT_SPEED);
        double sun = this.level().canSeeSky(this.blockPosition().above()) ? SunIntakeCondition.strength(this) : 0.0;
        AttributeModifier current = speed.getModifier(SUN_SPEED_ID);
        double bonus = SUN_SPEED_PEAK * sun;
        if (current != null && Math.abs(current.amount() - bonus) < 0.005) {
            return;
        }
        speed.removeModifier(SUN_SPEED_ID);
        if (bonus > 0.0) {
            speed.addTransientModifier(new AttributeModifier(
                SUN_SPEED_ID, bonus, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
    }

    public void setOwner(LivingEntity owner) {
        this.ownerUuid = owner == null ? null : owner.getUUID();
    }

    public LivingEntity getOwner() {
        return this.ownerUuid == null ? null : this.level().getPlayerByUUID(this.ownerUuid);
    }

    public boolean isOwnedBy(UUID uuid) {
        return uuid.equals(this.ownerUuid);
    }

    public boolean canMove() {
        return this.entityData.get(CAN_MOVE);
    }

    public ItemStack getOfferedFlower() {
        return this.entityData.get(OFFERED_FLOWER);
    }

    public static void despawnOwnedBy(MinecraftServer server, UUID ownerUuid) {
        for (ServerLevel level : server.getAllLevels()) {
            for (FloranHenchman henchman
                    : level.getEntities(MmsEntities.FLORAN_HENCHMAN, henchman -> henchman.isOwnedBy(ownerUuid))) {
                henchman.refundOnRemoval = false;
                henchman.crumble();
            }
        }
        LivingEntity owner = server.getPlayerList().getPlayer(ownerUuid);
        VariableIntPower charge = chargeOf(owner, null);
        if (charge != null && charge.getValue() != charge.getMin()) {
            charge.setValue(charge.getMin());
            PowerHolderComponent.syncPower(owner, charge.getType());
        }
    }

    public static VariableIntPower chargeOf(LivingEntity owner, PowerType<?> resource) {
        if (owner == null) {
            return null;
        }
        for (VariableIntPower power : PowerHolderComponent.getPowers(owner, VariableIntPower.class)) {
            if (power.getType() == resource || CHARGE_ID.equals(power.getType().getIdentifier())) {
                return power;
            }
        }
        return null;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(CAN_MOVE, true);
        builder.define(OFFERED_FLOWER, ItemStack.EMPTY);
    }

    @Override
    protected BodyRotationControl createBodyControl() {
        return new FrozenAwareBodyRotationControl(this);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        GroundPathNavigation navigation = new FrozenAwareNavigation(this, level);
        navigation.setCanFloat(true);
        return navigation;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new HenchmanFollowOwnerGoal(this, 1.2, 16.0f, 5.0f));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.addGoal(3, new MoveTowardsTargetGoal(this, 0.9, 32.0f));
        this.goalSelector.addGoal(4, new HenchmanFollowOwnerGoal(this, 1.1, 7.0f, 3.0f));
        this.goalSelector.addGoal(5, new OfferFlowerGoal(this));
        this.goalSelector.addGoal(6, new GolemRandomStrollInVillageGoal(this, 0.6));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(
            this,
            Mob.class,
            5,
            false,
            false,
            (living, serverLevel) -> living instanceof Enemy && !(living instanceof Creeper)));
    }

    @Override
    public void aiStep() {
        if (this.attackAnimationRemainingTicks > 0) {
            this.attackAnimationRemainingTicks--;
        }
        if (!this.level().isClientSide()) {
            boolean couldMove = this.canMove();
            boolean canMove = this.checkCanMove();
            if (canMove != couldMove) {
                if (canMove) {
                    this.makeSound(SoundEvents.CREAKING_UNFREEZE);
                } else {
                    this.stopInPlace();
                    this.makeSound(SoundEvents.CREAKING_FREEZE);
                }
                this.entityData.set(CAN_MOVE, canMove);
            }
        }
        super.aiStep();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            this.attackAnimationState.animateWhen(this.attackAnimationRemainingTicks > 0, this.tickCount);
        }
    }

    private boolean checkCanMove() {
        if (!(this.getTarget() instanceof Player player) || player == this.getOwner()) {
            return true;
        }
        if (this.distanceToSqr(player) > FREEZE_RANGE * FREEZE_RANGE
                || !LivingEntity.PLAYER_NOT_WEARING_DISGUISE_ITEM.test(player)) {
            return true;
        }
        return !this.isLookingAtMe(
            player,
            CROSSHAIR_TOLERANCE,
            true,
            false,
            this.getEyeY(),
            this.getY() + 0.5 * this.getScale(),
            (this.getEyeY() + this.getY()) / 2.0);
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        this.updateSunSpeed();
        this.age++;
        if (this.age >= LIFESPAN_TICKS) {
            this.crumble();
            return;
        }
        LivingEntity owner = this.getOwner();
        if (owner != null && this.distanceToSqr(owner) > LEASH_RANGE * LEASH_RANGE) {
            this.crumble();
            return;
        }
        if (this.age % TAUNT_INTERVAL == 0) {
            this.pullAggroOffOwner(level);
        }
    }

    @Override
    public void remove(Entity.RemovalReason reason) {
        if (this.level() instanceof ServerLevel && this.refundOnRemoval) {
            if (reason == Entity.RemovalReason.KILLED) {
                HenchmanLifecycle.scheduleRefund(this.level().getServer(), this.ownerUuid);
            } else if (reason == Entity.RemovalReason.DISCARDED) {
                this.refundNow();
            }
        }
        super.remove(reason);
    }

    /**
     * Bursts into jungle wood and leaves and vanishes with the creaking's death
     * cry, the way a creaking crumbles at sunrise.
     */
    public void crumble() {
        if (this.level() instanceof ServerLevel level) {
            AABB box = this.getBoundingBox();
            Vec3 center = box.getCenter();
            double spreadX = box.getXsize() * 0.3;
            double spreadY = box.getYsize() * 0.3;
            double spreadZ = box.getZsize() * 0.3;
            level.sendParticles(
                new BlockParticleOption(ParticleTypes.BLOCK_CRUMBLE, Blocks.JUNGLE_WOOD.defaultBlockState()),
                center.x,
                center.y,
                center.z,
                100,
                spreadX,
                spreadY,
                spreadZ,
                0.0);
            level.sendParticles(
                new BlockParticleOption(ParticleTypes.BLOCK_CRUMBLE, Blocks.JUNGLE_LEAVES.defaultBlockState()),
                center.x,
                center.y,
                center.z,
                10,
                spreadX,
                spreadY,
                spreadZ,
                0.0);
        }
        this.makeSound(this.getDeathSound());
        this.discard();
    }

    private void refundNow() {
        LivingEntity owner = this.getOwner();
        VariableIntPower charge = chargeOf(owner, null);
        if (charge != null && charge.getValue() < charge.getMax()) {
            charge.setValue(Math.min(charge.getMax(), charge.getValue() + CHARGE_COST));
            PowerHolderComponent.syncPower(owner, charge.getType());
        }
    }

    private void pullAggroOffOwner(ServerLevel level) {
        LivingEntity owner = this.getOwner();
        if (owner == null) {
            return;
        }
        AABB range = this.getBoundingBox().inflate(TAUNT_RADIUS);
        for (Mob mob : level.getEntitiesOfClass(Mob.class, range, this::taunts)) {
            if (mob.getTarget() == owner) {
                mob.setTarget(this);
            }
        }
    }

    private boolean taunts(Mob mob) {
        return mob instanceof Enemy && !(mob instanceof FloranHenchman);
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return target != this.getOwner() && super.canAttack(target);
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        if (!this.canMove() || !(target instanceof LivingEntity living)) {
            return false;
        }
        this.attackAnimationRemainingTicks = ATTACK_ANIMATION_TICKS;
        level.broadcastEntityEvent(this, ATTACK_EVENT);
        boolean hit = super.doHurtTarget(level, target);
        if (hit) {
            living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, SLOWNESS_TICKS, 0), this);
        }
        return hit;
    }

    @Override
    public void handleEntityEvent(byte event) {
        if (event == ATTACK_EVENT) {
            this.attackAnimationRemainingTicks = ATTACK_ANIMATION_TICKS;
            this.makeSound(SoundEvents.CREAKING_ATTACK);
        } else {
            super.handleEntityEvent(event);
        }
    }

    @Override
    public boolean isPushable() {
        return super.isPushable() && this.canMove();
    }

    @Override
    public void push(double x, double y, double z) {
        if (this.canMove()) {
            super.push(x, y, z);
        }
    }

    @Override
    public void knockback(double strength, double x, double z) {
        if (this.canMove()) {
            super.knockback(strength, x, z);
        }
    }

    @Override
    protected void updateWalkAnimation(float distance) {
        this.walkAnimation.update(Math.min(distance * 10.0f, 1.5f), 0.4f, 1.0f);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.CREAKING_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.CREAKING_SWAY;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.CREAKING_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.CREAKING_STEP, 0.15f, 1.0f);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        if (this.ownerUuid != null) {
            output.store("Owner", UUIDUtil.CODEC, this.ownerUuid);
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.ownerUuid = input.read("Owner", UUIDUtil.CODEC).orElse(null);
    }

    private static final class OfferFlowerGoal extends Goal {

        private static final int OFFER_CHANCE = 2000;
        private static final int OFFER_TICKS = 400;
        private static final double OFFER_RANGE = 6.0;
        private static final TargetingConditions OFFER_TARGET =
            TargetingConditions.forNonCombat().range(OFFER_RANGE);

        private final FloranHenchman henchman;
        private LivingEntity recipient;
        private int ticks;

        private OfferFlowerGoal(FloranHenchman henchman) {
            this.henchman = henchman;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (this.henchman.getTarget() != null
                    || !this.henchman.level().isBrightOutside()
                    || this.henchman.getRandom().nextInt(OFFER_CHANCE) != 0) {
                return false;
            }
            this.recipient = this.findRecipient();
            return this.recipient != null;
        }

        private LivingEntity findRecipient() {
            LivingEntity owner = this.henchman.getOwner();
            if (owner != null && this.henchman.distanceToSqr(owner) < OFFER_RANGE * OFFER_RANGE) {
                return owner;
            }
            return getServerLevel(this.henchman).getNearestEntity(
                EntityTypeTags.CANDIDATE_FOR_IRON_GOLEM_GIFT,
                OFFER_TARGET,
                this.henchman,
                this.henchman.getX(),
                this.henchman.getY(),
                this.henchman.getZ(),
                this.henchman.getBoundingBox().inflate(OFFER_RANGE, 2.0, OFFER_RANGE));
        }

        @Override
        public boolean canContinueToUse() {
            return this.ticks > 0 && this.henchman.getTarget() == null && this.recipient.isAlive();
        }

        @Override
        public void start() {
            this.ticks = this.adjustedTickDelay(OFFER_TICKS);
            ItemStack flower = BuiltInRegistries.ITEM.get(ItemTags.SMALL_FLOWERS)
                .flatMap(flowers -> flowers.getRandomElement(this.henchman.getRandom()))
                .map(holder -> new ItemStack(holder))
                .orElse(ItemStack.EMPTY);
            this.henchman.entityData.set(OFFERED_FLOWER, flower);
        }

        @Override
        public void stop() {
            ItemStack flower = this.henchman.getOfferedFlower();
            this.henchman.entityData.set(OFFERED_FLOWER, ItemStack.EMPTY);
            if (this.ticks <= 0 && !flower.isEmpty() && this.recipient.isAlive()
                    && this.henchman.distanceToSqr(this.recipient) < OFFER_RANGE * OFFER_RANGE) {
                this.henchman.level().addFreshEntity(new ItemEntity(
                    this.henchman.level(),
                    this.recipient.getX(),
                    this.recipient.getY(),
                    this.recipient.getZ(),
                    flower));
            }
            this.recipient = null;
        }

        @Override
        public void tick() {
            this.henchman.getLookControl().setLookAt(this.recipient, 30.0f, 30.0f);
            this.ticks--;
        }
    }

    private static final class FrozenAwareBodyRotationControl extends BodyRotationControl {

        private final FloranHenchman henchman;

        private FrozenAwareBodyRotationControl(FloranHenchman henchman) {
            super(henchman);
            this.henchman = henchman;
        }

        @Override
        public void clientTick() {
            if (this.henchman.canMove()) {
                super.clientTick();
            }
        }
    }

    private static final class FrozenAwareJumpControl extends JumpControl {

        private final FloranHenchman henchman;

        private FrozenAwareJumpControl(FloranHenchman henchman) {
            super(henchman);
            this.henchman = henchman;
        }

        @Override
        public void tick() {
            if (this.henchman.canMove()) {
                super.tick();
            } else {
                this.henchman.setJumping(false);
            }
        }
    }

    private static final class FrozenAwareLookControl extends LookControl {

        private final FloranHenchman henchman;

        private FrozenAwareLookControl(FloranHenchman henchman) {
            super(henchman);
            this.henchman = henchman;
        }

        @Override
        public void tick() {
            if (this.henchman.canMove()) {
                super.tick();
            }
        }
    }

    private static final class FrozenAwareMoveControl extends MoveControl {

        private final FloranHenchman henchman;

        private FrozenAwareMoveControl(FloranHenchman henchman) {
            super(henchman);
            this.henchman = henchman;
        }

        @Override
        public void tick() {
            if (this.henchman.canMove()) {
                super.tick();
            }
        }
    }

    private static final class FrozenAwareNavigation extends GroundPathNavigation {

        private final FloranHenchman henchman;

        private FrozenAwareNavigation(FloranHenchman henchman, Level level) {
            super(henchman, level);
            this.henchman = henchman;
        }

        @Override
        public void tick() {
            if (this.henchman.canMove()) {
                super.tick();
            }
        }
    }
}
