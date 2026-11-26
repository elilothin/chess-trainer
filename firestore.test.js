const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");
const ts = () => new Date(Date.now() - 1000);

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const DB_ID = "ai-studio-android-chessx-2766b97e-c698-4af2-a8a4-4f6f8dfe8439";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";
const CHARLIE_UID = "charlie_789";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function validProfile(uid) {
  return {
    userId: uid,
    displayName: "Laurence",
    campaignElo: 500,
    onlineElo: 800,
    selectedUniverse: "CLASSIQUE",
    selectedBoard: "CLASSIQUE",
    unlockedUniverses: ["CLASSIQUE"],
    westernWins: 0,
    greceWins: 0,
    japonWins: 0,
    basketWins: 0,
    futuristeWins: 0,
    islandLevel: 0,
    animationsEnabled: true,
    onlineWins: 0,
    onlineLosses: 0,
    onlineDraws: 0,
    createdAt: ts(),
    updatedAt: ts(),
  };
}

function validWaitingMatch(uid) {
  return {
    whitePlayerId: uid,
    whiteDisplayName: "Alice",
    whiteUniverse: "WESTERN",
    whiteElo: 800,
    boardUniverse: "WESTERN",
    timeControlMinutes: 5,
    whiteTimeMs: 300000,
    blackTimeMs: 300000,
    status: "waiting",
    fen: "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
    turn: "w",
    isCheck: false,
    moveCount: 0,
    memberUids: [uid],
    createdAt: ts(),
    updatedAt: ts(),
  };
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      databaseId: DB_ID,
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

test("Unauthenticated user: denied access to users and onlineMatches", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("onlineMatches").where("status", "==", "waiting").get());
});

test("User profile: owner can create and update valid profile; rejects ghost fields & cross-user read", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).set(validProfile(ALICE_UID)));
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).get());

  // Cross-user read denied
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());

  // Valid update
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      westernWins: 1,
      campaignElo: 600,
      updatedAt: ts(),
    })
  );

  // Ghost field update denied
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      isAdmin: true,
      updatedAt: ts(),
    })
  );

  // Invalid universe denied
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      selectedUniverse: "PIRATES",
      updatedAt: ts(),
    })
  );
});

test("MatchHistory: owner can append record; update/delete and cross-user denied", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const recRef = aliceDb.collection("users").doc(ALICE_UID).collection("matchHistory").doc("hist_1");

  await assertSucceeds(
    recRef.set({
      mode: "CAMPAIGN",
      opponentName: "Shérif du Saloon",
      opponentUniverse: "WESTERN",
      playerUniverse: "CLASSIQUE",
      result: "WIN",
      endReason: "CHECKMATE",
      eloDelta: 100,
      eloAfter: 600,
      movesCount: 24,
      timeControlMinutes: 0,
      createdAt: ts(),
    })
  );

  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("matchHistory").get());
  await assertFails(recRef.update({ result: "LOSS" }));
  await assertFails(recRef.delete());
});

test("OnlineMatches: full lobby create, query, join, move, checkmate terminal lock, and isolation", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const charlieDb = testEnv.authenticatedContext(CHARLIE_UID).firestore();

  const matchRef = aliceDb.collection("onlineMatches").doc("match_1");
  await assertSucceeds(matchRef.set(validWaitingMatch(ALICE_UID)));

  // Unfiltered list fails (rules are not filters)
  await assertFails(bobDb.collection("onlineMatches").get());

  // Filtered waiting lobby query succeeds
  await assertSucceeds(bobDb.collection("onlineMatches").where("status", "==", "waiting").get());

  // Bob joins match
  await assertSucceeds(
    bobDb.collection("onlineMatches").doc("match_1").update({
      blackPlayerId: BOB_UID,
      blackDisplayName: "Bob",
      blackUniverse: "JAPON",
      blackElo: 820,
      status: "active",
      memberUids: [ALICE_UID, BOB_UID],
      updatedAt: ts(),
    })
  );

  // Charlie cannot read or update active match
  await assertFails(charlieDb.collection("onlineMatches").doc("match_1").get());
  await assertFails(
    charlieDb.collection("onlineMatches").doc("match_1").update({
      status: "resigned",
      updatedAt: ts(),
    })
  );

  // Alice makes a move with capture
  await assertSucceeds(
    matchRef.update({
      fen: "rnbqkbnr/pppp1ppp/8/4p3/4P3/8/PPPP1PPP/RNBQKBNR w KQkq - 0 2",
      turn: "b",
      whiteTimeMs: 295000,
      blackTimeMs: 300000,
      lastMoveUci: "e2e4",
      lastCapturePiece: "Q",
      lastCaptureVictim: "p",
      lastCaptureUniverse: "WESTERN",
      isCheck: false,
      moveCount: 1,
      updatedAt: ts(),
    })
  );

  // Alice wins by checkmate (terminal lock)
  await assertSucceeds(
    matchRef.update({
      status: "checkmate",
      winnerId: ALICE_UID,
      winnerUniverse: "WESTERN",
      updatedAt: ts(),
    })
  );

  // Terminal lock rejects any further updates
  await assertFails(
    matchRef.update({
      status: "active",
      updatedAt: ts(),
    })
  );
});
