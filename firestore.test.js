const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_pai_01";
const BOB_UID = "bob_pai_02";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function validProgressPayload(uid, offsetMs = 1000) {
  const ts = new Date(Date.now() - offsetMs);
  return {
    userId: uid,
    displayName: "Ahmad Luqman",
    highestUnlockedStage: 3,
    completedStages: [1, 2],
    exploredObjects: ["papan_tulis", "mushaf"],
    selectedStrategies: ["Problem Based Learning"],
    studentAnalysisDone: false,
    localWisdomDone: false,
    strategyChoiceDone: false,
    caseAnalysisSummary: "",
    quizScore: 0,
    quizCompleted: false,
    reflectionText: "",
    competencyRecommendation: "",
    createdAt: ts,
    updatedAt: ts,
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

test("Unauthenticated user cannot read or write student progress, AI records, or notifications", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).set(validProgressPayload(ALICE_UID))
  );
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("ai_records").get()
  );
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("notifications").get()
  );
});

test("Authenticated student can create, read, and partially update own progress", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const userRef = aliceDb.collection("users").doc(ALICE_UID);

  await assertSucceeds(userRef.set(validProgressPayload(ALICE_UID, 5000)));
  await assertSucceeds(userRef.get());

  await assertSucceeds(
    userRef.update({
      highestUnlockedStage: 5,
      completedStages: [1, 2, 3, 4],
      studentAnalysisDone: true,
      quizScore: 90,
      quizCompleted: true,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Reject immutable createdAt modification
  await assertFails(
    userRef.update({
      createdAt: new Date(Date.now() - 500),
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Reject ghost field injection
  await assertFails(
    aliceDb.collection("users").doc("extra_doc").set({
      ...validProgressPayload(ALICE_UID),
      id: "extra_doc",
    })
  );

  // Reject future timestamp tampering
  await assertFails(
    userRef.update({
      quizScore: 100,
      updatedAt: new Date(Date.now() + 60000),
    })
  );
});

test("Cross-user isolation blocks reading or mutating another student's data", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    const db = context.firestore();
    await db.collection("users").doc(BOB_UID).set(validProgressPayload(BOB_UID, 5000));
    await db
      .collection("users")
      .doc(BOB_UID)
      .collection("ai_records")
      .doc("rec_1")
      .set({
        userId: BOB_UID,
        toolType: "CASE_STUDY",
        promptText: "Kasus kelas PAI",
        responseText: "Analisis skenario",
        sourceMode: "GEMINI_AI",
        difficultyLevel: "MENENGAH",
        createdAt: new Date(Date.now() - 5000),
        updatedAt: new Date(Date.now() - 5000),
      });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("users").doc(BOB_UID).get());
  await assertFails(
    aliceDb.collection("users").doc(BOB_UID).update({ quizScore: 10 })
  );
  await assertFails(
    aliceDb.collection("users").doc(BOB_UID).collection("ai_records").get()
  );
});

test("Authenticated owner can create and list AI records and real-time notifications", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const ts = new Date(Date.now() - 2000);

  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("ai_records")
      .doc("rec_01")
      .set({
        userId: ALICE_UID,
        toolType: "AI_TUTOR",
        promptText: "Kapan CTL tepat digunakan?",
        responseText: "CTL tepat saat mengaitkan materi PAI dengan realitas sosial.",
        sourceMode: "GEMINI_AI",
        difficultyLevel: "DASAR",
        createdAt: ts,
        updatedAt: ts,
      })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("ai_records").get()
  );

  // Reject invalid enum value
  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("ai_records")
      .doc("rec_02")
      .set({
        userId: ALICE_UID,
        toolType: "INVALID_TYPE",
        promptText: "Test",
        responseText: "Test",
        sourceMode: "GEMINI_AI",
        difficultyLevel: "DASAR",
        createdAt: ts,
        updatedAt: ts,
      })
  );

  const notifRef = aliceDb
    .collection("users")
    .doc(ALICE_UID)
    .collection("notifications")
    .doc("notif_01");

  await assertSucceeds(
    notifRef.set({
      userId: ALICE_UID,
      title: "Tahap 3 Selesai",
      message: "Anda telah mengeksplorasi Ruang Kelas Virtual 3D.",
      stageNumber: 3,
      readFlag: false,
      createdAt: ts,
      updatedAt: ts,
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("notifications").get()
  );

  await assertSucceeds(
    notifRef.update({
      readFlag: true,
      updatedAt: new Date(Date.now() - 1000),
    })
  );
});
