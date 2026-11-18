package info.mudbourn.mmsorigins.entity;

import info.mudbourn.mmsorigins.MmsOrigins;
import info.mudbourn.mmsorigins.MmsOriginsPowers;
import info.mudbourn.mmsorigins.power.condition.SunIntakeCondition;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.VariableIntPower;
import java.util.EnumSet;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
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
import net.minecraft.world.item.Item;
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
    private static final EntityDataAccessor<Boolean> RUNNING =
        SynchedEntityData.defineId(FloranHenchman.class, EntityDataSerializers.BOOLEAN);
    private static final TagKey<Item> SMALL_FLOWERS =
        TagKey.create(Registries.ITEM, Identifier.withDefaultNamespace("small_flowers"));
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
    private static final int PILOTED_AGE_RATE = 2;
    private static final double PILOTED_SPEED_SCALE = 0.75;
    private static final double RUN_SPEED_SCALE = 1.6;
    private static final int RUN_TICKS = 60;
    private static final int RUN_COOLDOWN_TICKS = 200;
    private static final int RIDER_ATTACK_COOLDOWN = 20;
    private static final double RIDER_REACH = 4.0;
    private static final int MOUNT_MORPH_TICKS = 10;
    private static final int MOUNT_MORPH_FX_INTERVAL = 4;
    private static final float PILOTED_WALK_CAP = 4.0f;
    private static final int PILOTED_REFUND = CHARGE_COST / 2;
    private static final double SEAT_HEIGHT = 1.9;
    private static final double SEAT_BACK_OFFSET = 0.3;

    public final AnimationState attackAnimationState = new AnimationState();
    private int attackAnimationRemainingTicks;
    private UUID ownerUuid;
    private int age;
    private boolean refundOnRemoval = true;
    private int runTicks;
    private int runCooldown;
    private int lastRiderAttack;
    private int mountTicks;

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
        builder.define(RUNNING, false);
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
        if (this.level() instanceof ServerLevel level) {
            this.serveOwner(level);
        } else {
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

    /** Plays the leaf-form morph's particles and closing sound over the first ticks of a mount. */
    private void tickMountMorph(ServerLevel level) {
        if (this.mountTicks <= 0) {
            return;
        }
        this.mountTicks--;
        if (this.mountTicks % MOUNT_MORPH_FX_INTERVAL == 0) {
            level.sendParticles(
                ParticleTypes.COMPOSTER,
                this.getX(),
                this.getY() + this.getBbHeight() * 0.7,
                this.getZ(),
                12,
                0.4,
                0.6,
                0.4,
                0.05);
        }
        if (this.mountTicks == 0) {
            this.playSound(SoundEvents.AZALEA_LEAVES_PLACE, 1.0f, 1.2f);
        }
    }

    private void serveOwner(ServerLevel level) {
        this.updateSunSpeed();
        LivingEntity pilot = this.getControllingPassenger();
        this.tickMountMorph(level);
        this.tickRun(pilot);
        if (pilot != null) {
            this.setTarget(null);
            this.getNavigation().stop();
            if (!MmsOriginsPowers.leafFormComplete(pilot)) {
                pilot.stopRiding();
            }
        }
        this.age += pilot == null ? 1 : PILOTED_AGE_RATE;
        if (this.age >= LIFESPAN_TICKS) {
            this.crumble();
            return;
        }
        LivingEntity owner = this.getOwner();
        if (owner != null && this.distanceToSqr(owner) > LEASH_RANGE * LEASH_RANGE) {
            this.crumble();
            return;
        }
        if (this.tickCount % TAUNT_INTERVAL == 0) {
            this.pullAggroOffOwner(level);
        }
    }

    private void tickRun(LivingEntity pilot) {
        if (this.runCooldown > 0) {
            this.runCooldown--;
        }
        boolean wantsRun = pilot instanceof ServerPlayer rider && rider.getLastClientInput().sprint();
        if (this.isRunning()) {
            this.runTicks++;
            if (!wantsRun || this.runTicks >= RUN_TICKS) {
                this.entityData.set(RUNNING, false);
                this.runCooldown = RUN_COOLDOWN_TICKS;
                if (pilot != null) {
                    this.makeSound(SoundEvents.CREAKING_SWAY);
                }
            }
        } else if (wantsRun && this.runCooldown <= 0) {
            this.runTicks = 0;
            this.entityData.set(RUNNING, true);
        }
    }

    public boolean isRunning() {
        return this.entityData.get(RUNNING);
    }

    /** Swings for the rider, striking what they aim at within the golem's reach, or the given target if one was hit. */
    public void strikeFor(Player rider, Entity aimed) {
        if (!(this.level() instanceof ServerLevel level)
                || this.tickCount - this.lastRiderAttack < RIDER_ATTACK_COOLDOWN) {
            return;
        }
        this.lastRiderAttack = this.tickCount;
        LivingEntity target = this.reachable(aimed) ? (LivingEntity) aimed : this.aimedTarget(rider);
        if (target == null) {
            this.attackAnimationRemainingTicks = ATTACK_ANIMATION_TICKS;
            level.broadcastEntityEvent(this, ATTACK_EVENT);
            return;
        }
        this.doHurtTarget(level, target);
    }

    private boolean reachable(Entity entity) {
        return entity instanceof LivingEntity living
            && entity != this
            && !this.hasPassenger(entity)
            && this.canAttack(living)
            && this.getBoundingBox().inflate(RIDER_REACH).intersects(entity.getBoundingBox());
    }

    private LivingEntity aimedTarget(Player rider) {
        Vec3 eye = rider.getEyePosition();
        double range = Math.sqrt(this.distanceToSqr(rider)) + this.getBbWidth() + RIDER_REACH;
        Vec3 end = eye.add(rider.getViewVector(1.0f).scale(range));
        AABB sweep = rider.getBoundingBox().expandTowards(end.subtract(eye)).inflate(1.0);
        LivingEntity best = null;
        double bestDistance = Double.MAX_VALUE;
        for (Entity candidate : this.level().getEntities(this, sweep, this::reachable)) {
            Optional<Vec3> hit = candidate.getBoundingBox().inflate(candidate.getPickRadius() + 0.3).clip(eye, end);
            if (hit.isPresent() && eye.distanceToSqr(hit.get()) < bestDistance) {
                bestDistance = eye.distanceToSqr(hit.get());
                best = (LivingEntity) candidate;
            }
        }
        return best;
    }

    @Override
    public boolean isPickable() {
        return super.isPickable()
            && !(this.getControllingPassenger() instanceof Player rider && rider.isLocalPlayer());
    }

    @Override
    public boolean isEffectiveAi() {
        return this.getControllingPassenger() == null && super.isEffectiveAi();
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand == InteractionHand.MAIN_HAND
                && (this.level().isClientSide() || this.isOwnedBy(player.getUUID()))
                && MmsOriginsPowers.leafFormComplete(player)
                && !this.isVehicle()
                && !player.isPassenger()) {
            if (!this.level().isClientSide() && player.startRiding(this)) {
                this.mountTicks = MOUNT_MORPH_TICKS;
                this.playSound(SoundEvents.AZALEA_LEAVES_PLACE, 1.0f, 0.9f);
            }
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public LivingEntity getControllingPassenger() {
        return this.getFirstPassenger() instanceof Player rider ? rider : null;
    }

    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity passenger, EntityDimensions dimensions, float scale) {
        return new Vec3(0.0, SEAT_HEIGHT * scale, -SEAT_BACK_OFFSET * scale)
            .yRot(-this.yBodyRot * Mth.DEG_TO_RAD);
    }

    @Override
    protected Vec3 getRiddenInput(Player rider, Vec3 travelVector) {
        float forward = rider.zza;
        return new Vec3(rider.xxa * 0.5, 0.0, forward <= 0.0f ? forward * 0.25f : forward);
    }

    @Override
    protected float getRiddenSpeed(Player rider) {
        double scale = this.isRunning() ? PILOTED_SPEED_SCALE * RUN_SPEED_SCALE : PILOTED_SPEED_SCALE;
        return (float) (this.getAttributeValue(Attributes.MOVEMENT_SPEED) * scale);
    }

    @Override
    protected void tickRidden(Player rider, Vec3 input) {
        super.tickRidden(rider, input);
        this.setRot(rider.getYRot(), rider.getXRot() * 0.5f);
        this.yRotO = this.getYRot();
        this.yBodyRot = this.getYRot();
        this.yHeadRot = this.getYRot();
    }

    @Override
    public void die(DamageSource source) {
        if (this.level() instanceof ServerLevel) {
            this.refundPiloted();
        }
        super.die(source);
    }

    @Override
    public void remove(Entity.RemovalReason reason) {
        if (this.level() instanceof ServerLevel && this.refundOnRemoval && !this.refundPiloted()) {
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
        this.refund(CHARGE_COST);
    }

    private boolean refundPiloted() {
        if (!this.refundOnRemoval || this.getControllingPassenger() == null) {
            return false;
        }
        this.refundOnRemoval = false;
        this.refund(PILOTED_REFUND);
        return true;
    }

    private void refund(int amount) {
        LivingEntity owner = this.getOwner();
        VariableIntPower charge = chargeOf(owner, null);
        if (charge != null && charge.getValue() < charge.getMax()) {
            charge.setValue(Math.min(charge.getMax(), charge.getValue() + amount));
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
        return target != this.getOwner() && !this.isPackmate(target) && super.canAttack(target);
    }

    @Override
    protected boolean considersEntityAsAlly(Entity other) {
        return this.isPackmate(other) || super.considersEntityAsAlly(other);
    }

    /** Whether the other entity is a Grovekeeper serving the same summoner. */
    private boolean isPackmate(Entity other) {
        return other instanceof FloranHenchman henchman
            && this.ownerUuid != null
            && henchman.isOwnedBy(this.ownerUuid);
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        if ((!this.canMove() && !this.isVehicle()) || !(target instanceof LivingEntity living)) {
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
    public void knockback(double strength, double x, double z, DamageSource source, float resistance, boolean sendPacket) {
        if (this.canMove()) {
            super.knockback(strength, x, z, source, resistance, sendPacket);
        }
    }

    @Override
    protected void updateWalkAnimation(float distance) {
        float cap = this.getControllingPassenger() == null ? 1.5f : PILOTED_WALK_CAP;
        this.walkAnimation.update(Math.min(distance * 10.0f, cap), 0.4f, 1.0f);
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
            ItemStack flower = BuiltInRegistries.ITEM.get(SMALL_FLOWERS)
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
