const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const DATABASE_ID = "ai-studio-android-sereneoa-ed08fee0-3a60-4cd8-8d3d-9aa72a8cf9c3";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function tsNow() {
  return new Date(Date.now() - 1000);
}

function validProfile(uid) {
  return {
    userId: uid,
    displayName: "Elena Vance",
    email: "elena@example.com",
    membershipTier: "SAGE_MEMBER",
    wellnessPoints: 250,
    preferredPressure: "Medium",
    aromatherapyNote: "Eucalyptus & Bergamot",
    favoriteTreatmentIds: ["sage_botanical_facial"],
    createdAt: tsNow(),
    updatedAt: tsNow(),
  };
}

function validBooking(uid) {
  return {
    userId: uid,
    treatmentId: "sage_botanical_facial",
    treatmentTitle: "Sage Botanical Renewal Facial",
    treatmentCategory: "Facials",
    therapistId: "claire_moreau",
    therapistName: "Claire Moreau",
    dateLabel: "Fri, Nov 20",
    dateIso: "2026-11-20",
    timeSlot: "2:30 PM",
    durationMinutes: 75,
    priceUsd: 210,
    addOns: ["Jade Gua Sha Lift"],
    specialRequests: "Warm table blanket please",
    status: "confirmed",
    createdAt: tsNow(),
    updatedAt: tsNow(),
  };
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user cannot read or write profiles or bookings", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore(DATABASE_ID);
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).set(validProfile(ALICE_UID)));
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("bookings").get());
});

test("Owner can create and update their own profile, but shadow fields fail", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore(DATABASE_ID);
  const profileRef = aliceDb.collection("users").doc(ALICE_UID);

  await assertSucceeds(profileRef.set(validProfile(ALICE_UID)));
  await assertSucceeds(profileRef.get());

  // Valid update of favorites
  await assertSucceeds(
    profileRef.update({
      favoriteTreatmentIds: ["sage_botanical_facial", "golden_hammam_ritual"],
      updatedAt: tsNow(),
    })
  );

  // Shadow update fails
  await assertFails(
    profileRef.update({
      isAdmin: true,
      updatedAt: tsNow(),
    })
  );
});

test("Cross-user access to profile and bookings is denied", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore(DATABASE_ID);
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore(DATABASE_ID);

  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).set(validProfile(ALICE_UID)));
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("bookings").doc("bk_1").set(validBooking(ALICE_UID))
  );

  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("bookings").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("bookings").doc("bk_1").get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("bookings").doc("bk_2").set(validBooking(BOB_UID))
  );
});

test("Owner can create, list, reschedule, and cancel bookings; cancelled bookings are locked", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore(DATABASE_ID);
  const bookingRef = aliceDb.collection("users").doc(ALICE_UID).collection("bookings").doc("bk_100");

  await assertSucceeds(bookingRef.set(validBooking(ALICE_UID)));
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("bookings").get());

  // Reschedule confirmed booking
  await assertSucceeds(
    bookingRef.update({
      dateLabel: "Sat, Nov 21",
      dateIso: "2026-11-21",
      timeSlot: "4:00 PM",
      specialRequests: "Extra chamomile tea",
      updatedAt: tsNow(),
    })
  );

  // Cancel booking
  await assertSucceeds(
    bookingRef.update({
      status: "cancelled",
      updatedAt: tsNow(),
    })
  );

  // Terminal state lock: cannot modify after cancelled
  await assertFails(
    bookingRef.update({
      status: "confirmed",
      updatedAt: tsNow(),
    })
  );
});

function validReview(uid, bookingId) {
  return {
    userId: uid,
    bookingId: bookingId,
    targetType: "BOTH",
    treatmentId: "sage_botanical_facial",
    treatmentTitle: "Sage Botanical Renewal Facial",
    therapistId: "claire_moreau",
    therapistName: "Claire Moreau",
    rating: 5,
    comment: "Remarkably restorative facial and calm botanical suite.",
    reviewerName: "Elena Vance",
    createdAt: tsNow(),
    updatedAt: tsNow(),
  };
}

test("Reviews require existing non-cancelled booking, valid bounds, and cross-user isolation", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore(DATABASE_ID);
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore(DATABASE_ID);
  const unauthDb = testEnv.unauthenticatedContext().firestore(DATABASE_ID);

  // Creating a review without a matching booking fails
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("reviews").doc("missing_bk").set(validReview(ALICE_UID, "missing_bk"))
  );

  // Create a real booking and transition a second booking to cancelled
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("bookings").doc("bk_active").set(validBooking(ALICE_UID))
  );
  const cancelRef = aliceDb.collection("users").doc(ALICE_UID).collection("bookings").doc("bk_cancel");
  await assertSucceeds(cancelRef.set(validBooking(ALICE_UID)));
  await assertSucceeds(cancelRef.update({ status: "cancelled", updatedAt: tsNow() }));

  // Reviewing cancelled booking fails
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("reviews").doc("bk_cancel").set(validReview(ALICE_UID, "bk_cancel"))
  );

  // Out-of-bounds rating fails
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("reviews").doc("bk_active").set({
      ...validReview(ALICE_UID, "bk_active"),
      rating: 6,
    })
  );

  // Valid review succeeds
  const reviewRef = aliceDb.collection("users").doc(ALICE_UID).collection("reviews").doc("bk_active");
  await assertSucceeds(reviewRef.set(validReview(ALICE_UID, "bk_active")));
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("reviews").get());

  // Valid update of rating & comment succeeds; immutable bookingId update fails
  await assertSucceeds(
    reviewRef.update({
      targetType: "STAFF",
      rating: 4,
      comment: "Updated feedback for Claire Moreau.",
      updatedAt: tsNow(),
    })
  );
  await assertFails(
    reviewRef.update({
      bookingId: "other_bk",
      updatedAt: tsNow(),
    })
  );

  // Cross-user and unauthenticated reads/writes fail
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("reviews").doc("bk_active").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("reviews").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("reviews").get());
});

test("Refer a Friend: enforces unique code lookup, blocks self-referral and double redemption, allows atomic reward claim", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore(DATABASE_ID);
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore(DATABASE_ID);
  const unauthDb = testEnv.unauthenticatedContext().firestore(DATABASE_ID);
  const aliceCode = "SALONX-K9M2P4";

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      ...validProfile(ALICE_UID),
      referralCode: aliceCode,
      claimedReferralPoints: 0,
    })
  );
  await assertSucceeds(
    aliceDb.collection("referral_codes").doc(aliceCode).set({
      code: aliceCode,
      ownerUid: ALICE_UID,
      ownerName: "Elena Vance",
      successfulReferrals: 0,
      pointsEarned: 0,
      createdAt: tsNow(),
      updatedAt: tsNow(),
    })
  );
  await assertSucceeds(
    bobDb.collection("users").doc(BOB_UID).set({
      ...validProfile(BOB_UID),
      displayName: "Marcus Sterling",
      referralCode: "SALONX-B7N3Q8",
      claimedReferralPoints: 0,
    })
  );

  // Listing all referral codes or unauthenticated lookup is denied
  await assertFails(bobDb.collection("referral_codes").get());
  await assertFails(unauthDb.collection("referral_codes").doc(aliceCode).get());

  // Self-referral fails
  await assertFails(
    aliceDb.collection("referral_redemptions").doc(ALICE_UID).set({
      refereeUid: ALICE_UID,
      refereeName: "Elena Vance",
      referrerUid: ALICE_UID,
      referralCode: aliceCode,
      rewardPointsReferrer: 150,
      rewardPointsReferee: 100,
      createdAt: tsNow(),
    })
  );

  // Atomic batch redemption by Bob succeeds
  const batch = bobDb.batch();
  batch.set(bobDb.collection("referral_redemptions").doc(BOB_UID), {
    refereeUid: BOB_UID,
    refereeName: "Marcus Sterling",
    referrerUid: ALICE_UID,
    referralCode: aliceCode,
    rewardPointsReferrer: 150,
    rewardPointsReferee: 100,
    createdAt: tsNow(),
  });
  batch.update(bobDb.collection("referral_codes").doc(aliceCode), {
    successfulReferrals: 1,
    pointsEarned: 150,
    updatedAt: tsNow(),
  });
  batch.update(bobDb.collection("users").doc(BOB_UID), {
    wellnessPoints: 350,
    membershipTier: "SAGE_MEMBER",
    redeemedReferralCode: aliceCode,
    updatedAt: tsNow(),
  });
  await assertSucceeds(batch.commit());

  // Bob cannot redeem a second time
  await assertFails(
    bobDb.collection("referral_redemptions").doc(BOB_UID).set({
      refereeUid: BOB_UID,
      refereeName: "Marcus Sterling",
      referrerUid: ALICE_UID,
      referralCode: aliceCode,
      rewardPointsReferrer: 150,
      rewardPointsReferee: 100,
      createdAt: tsNow(),
    })
  );

  // Alice can query her referral history & claim earned +150 points
  await assertSucceeds(
    aliceDb.collection("referral_redemptions").where("referrerUid", "==", ALICE_UID).get()
  );
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      wellnessPoints: 400,
      membershipTier: "SAGE_MEMBER",
      claimedReferralPoints: 150,
      updatedAt: tsNow(),
    })
  );

  // Over-claiming un-earned referral points fails
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      wellnessPoints: 1000,
      membershipTier: "GOLD_SANCTUARY",
      claimedReferralPoints: 750,
      updatedAt: tsNow(),
    })
  );
});


