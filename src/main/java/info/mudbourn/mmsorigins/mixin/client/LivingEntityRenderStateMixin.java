package info.mudbourn.mmsorigins.mixin.client;

import info.mudbourn.mmsorigins.client.GuardianBeamState;
import info.mudbourn.mmsorigins.client.LeafFormState;
import info.mudbourn.mmsorigins.client.PhantomFlightPose;
import info.mudbourn.mmsorigins.client.PhantomFormState;
import info.mudbourn.mmsorigins.client.ZombieShakeState;
import info.mudbourn.mmsorigins.client.fur.FurState;
import info.mudbourn.mmsorigins.client.wings.WingState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Storage for {@link FurState}, {@link WingState}, {@link ZombieShakeState} and
 * {@link GuardianBeamState}, {@link LeafFormState} and {@link PhantomFormState} on
 * every living render state.
 *
 * <p>Stamped on {@code PlayerRenderer} extraction and cleared on every other entity's,
 * since render states are pooled and reused.
 */
@Mixin(LivingEntityRenderState.class)
public abstract class LivingEntityRenderStateMixin implements FurState, WingState, ZombieShakeState, GuardianBeamState, LeafFormState, PhantomFormState {

    @Unique
    private Identifier mmsOrigins$furOrigin;

    @Unique
    private Identifier mmsOrigins$tailOption;

    @Unique
    private boolean mmsOrigins$clawsOut;

    @Unique
    private boolean mmsOrigins$zombieShaking;

    @Unique
    private Identifier mmsOrigins$wingOption;

    @Unique
    private float mmsOrigins$wingFlapDegrees;

    @Override
    public Identifier mmsOrigins$furOrigin() {
        return this.mmsOrigins$furOrigin;
    }

    @Override
    public void mmsOrigins$setFurOrigin(Identifier origin) {
        this.mmsOrigins$furOrigin = origin;
    }

    @Override
    public Identifier mmsOrigins$tailOption() {
        return this.mmsOrigins$tailOption;
    }

    @Override
    public void mmsOrigins$setTailOption(Identifier option) {
        this.mmsOrigins$tailOption = option;
    }

    @Override
    public boolean mmsOrigins$clawsOut() {
        return this.mmsOrigins$clawsOut;
    }

    @Override
    public void mmsOrigins$setClawsOut(boolean out) {
        this.mmsOrigins$clawsOut = out;
    }

    @Override
    public Identifier mmsOrigins$wingOption() {
        return this.mmsOrigins$wingOption;
    }

    @Override
    public void mmsOrigins$setWingOption(Identifier option) {
        this.mmsOrigins$wingOption = option;
    }

    @Override
    public float mmsOrigins$wingFlapDegrees() {
        return this.mmsOrigins$wingFlapDegrees;
    }

    @Override
    public void mmsOrigins$setWingFlapDegrees(float degrees) {
        this.mmsOrigins$wingFlapDegrees = degrees;
    }

    @Override
    public boolean mmsOrigins$zombieShaking() {
        return this.mmsOrigins$zombieShaking;
    }

    @Override
    public void mmsOrigins$setZombieShaking(boolean shaking) {
        this.mmsOrigins$zombieShaking = shaking;
    }

    @Unique
    private Vec3 mmsOrigins$beamVector;

    @Unique
    private float mmsOrigins$beamHeight;

    @Unique
    private float mmsOrigins$beamTime;

    @Unique
    private float mmsOrigins$beamScale;

    @Override
    public Vec3 mmsOrigins$beamVector() {
        return this.mmsOrigins$beamVector;
    }

    @Override
    public void mmsOrigins$setBeamVector(Vec3 vector) {
        this.mmsOrigins$beamVector = vector;
    }

    @Override
    public float mmsOrigins$beamHeight() {
        return this.mmsOrigins$beamHeight;
    }

    @Override
    public void mmsOrigins$setBeamHeight(float height) {
        this.mmsOrigins$beamHeight = height;
    }

    @Override
    public float mmsOrigins$beamTime() {
        return this.mmsOrigins$beamTime;
    }

    @Override
    public void mmsOrigins$setBeamTime(float time) {
        this.mmsOrigins$beamTime = time;
    }

    @Override
    public float mmsOrigins$beamScale() {
        return this.mmsOrigins$beamScale;
    }

    @Override
    public void mmsOrigins$setBeamScale(float scale) {
        this.mmsOrigins$beamScale = scale;
    }

    @Unique
    private boolean mmsOrigins$leafForm;

    @Override
    public boolean mmsOrigins$leafForm() {
        return this.mmsOrigins$leafForm;
    }

    @Override
    public void mmsOrigins$setLeafForm(boolean leafForm) {
        this.mmsOrigins$leafForm = leafForm;
    }

    @Unique
    private Vec3 mmsOrigins$leafTail = Vec3.ZERO;

    @Override
    public Vec3 mmsOrigins$leafTail() {
        return this.mmsOrigins$leafTail;
    }

    @Override
    public void mmsOrigins$setLeafTail(Vec3 tail) {
        this.mmsOrigins$leafTail = tail;
    }

    @Unique
    private boolean mmsOrigins$phantomForm;

    @Override
    public boolean mmsOrigins$phantomForm() {
        return this.mmsOrigins$phantomForm;
    }

    @Override
    public void mmsOrigins$setPhantomForm(boolean phantomForm) {
        this.mmsOrigins$phantomForm = phantomForm;
    }

    @Unique
    private PhantomFlightPose.Angles mmsOrigins$phantomPose;

    @Override
    public PhantomFlightPose.Angles mmsOrigins$phantomPose() {
        return this.mmsOrigins$phantomPose;
    }

    @Override
    public void mmsOrigins$setPhantomPose(PhantomFlightPose.Angles pose) {
        this.mmsOrigins$phantomPose = pose;
    }
}
