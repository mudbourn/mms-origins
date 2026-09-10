package info.mudbourn.mmsorigins.entity;

import info.mudbourn.mmsorigins.MmsOrigins;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.VariableIntPower;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.ZombieAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;

/**
 * A floran's summoned henchman: a plant-grown zombie that fights for its
 * summoner and never turns on them.
 *
 * <p>It is sturdier and hits harder than a wild zombie because it is meant to
 * live under the floran's growth aura rather than on its own, and it does not
 * burn in daylight or drown into a husk. Every blow it lands saps the victim
 * with slowness, and its voice and footfalls are dry wood. Its owner is stored
 * by uuid so it can follow them and is barred from ever targeting them.
 */
public class FloranHenchman extends Zombie {

    private static final int SLOWNESS_TICKS = 100;
    private static final int LIFESPAN_TICKS = 2400;
    private static final int TAUNT_INTERVAL = 20;
    private static final double TAUNT_RADIUS = 16.0;
    private static final double LEASH_RANGE = 32.0;
    private static final Identifier CHARGE_ID =
        Identifier.fromNamespaceAndPath(MmsOrigins.MOD_ID, "sun_power");
    public static final int CHARGE_COST = 20;

    private UUID ownerUuid;
    private int age;
    private boolean refundOnRemoval = true;

    public FloranHenchman(EntityType<? extends Zombie> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Zombie.createAttributes()
            .add(Attributes.MAX_HEALTH, 28.0)
            .add(Attributes.ATTACK_DAMAGE, 5.0)
            .add(Attributes.ARMOR, 4.0)
            .add(Attributes.MOVEMENT_SPEED, 0.24)
            .add(Attributes.FOLLOW_RANGE, 24.0)
            .add(Attributes.SPAWN_REINFORCEMENTS_CHANCE, 0.0);
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

    public static void despawnOwnedBy(MinecraftServer server, UUID ownerUuid) {
        for (ServerLevel level : server.getAllLevels()) {
            for (FloranHenchman henchman
                    : level.getEntities(MmsEntities.FLORAN_HENCHMAN, henchman -> henchman.isOwnedBy(ownerUuid))) {
                henchman.refundOnRemoval = false;
                henchman.discard();
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
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new HenchmanFollowOwnerGoal(this, 1.2, 16.0f, 5.0f));
        this.goalSelector.addGoal(2, new ZombieAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(3, new HenchmanFollowOwnerGoal(this, 1.1, 7.0f, 3.0f));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(
            this,
            LivingEntity.class,
            10,
            true,
            false,
            (living, serverLevel) -> living instanceof Enemy && !(living instanceof FloranHenchman)));
    }

    @Override
    protected void addBehaviourGoals() {
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        this.age++;
        if (this.age >= LIFESPAN_TICKS) {
            this.discard();
            return;
        }
        LivingEntity owner = this.getOwner();
        if (owner != null && this.distanceToSqr(owner) > LEASH_RANGE * LEASH_RANGE) {
            this.discard();
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
        boolean hit = super.doHurtTarget(level, target);
        if (hit && target instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, SLOWNESS_TICKS, 0), this);
        }
        return hit;
    }

    @Override
    protected boolean isSunSensitive() {
        return false;
    }

    @Override
    protected boolean convertsInWater() {
        return false;
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
    protected SoundEvent getStepSound() {
        return SoundEvents.CREAKING_STEP;
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
}
