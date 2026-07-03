-- GymSync Database Schema
-- PostgreSQL Migration 001: Initial Schema

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Users table (exactly two users max)
CREATE TABLE IF NOT EXISTS "Users" (
    "Id" UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    "Username" VARCHAR(50) NOT NULL UNIQUE,
    "DisplayName" VARCHAR(100) NOT NULL,
    "Email" VARCHAR(200) NOT NULL UNIQUE,
    "PasswordHash" TEXT NOT NULL,
    "ProfilePhotoUrl" TEXT,
    "UnitSystem" INTEGER NOT NULL DEFAULT 0,
    "Status" INTEGER NOT NULL DEFAULT 0,
    "IsAdmin" BOOLEAN NOT NULL DEFAULT FALSE,
    "IsPartnerLinked" BOOLEAN NOT NULL DEFAULT FALSE,
    "PartnerId" UUID REFERENCES "Users"("Id") ON DELETE SET NULL,
    "CurrentStreak" INTEGER NOT NULL DEFAULT 0,
    "LongestStreak" INTEGER NOT NULL DEFAULT 0,
    "LastWorkoutAt" TIMESTAMPTZ,
    "LastActiveAt" TIMESTAMPTZ,
    "CreatedAt" TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    "UpdatedAt" TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Refresh Tokens
CREATE TABLE IF NOT EXISTS "RefreshTokens" (
    "Id" UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    "UserId" UUID NOT NULL REFERENCES "Users"("Id") ON DELETE CASCADE,
    "Token" TEXT NOT NULL UNIQUE,
    "ExpiresAt" TIMESTAMPTZ NOT NULL,
    "IsRevoked" BOOLEAN NOT NULL DEFAULT FALSE,
    "CreatedAt" TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    "RevokedAt" TIMESTAMPTZ
);

-- Invite Tokens
CREATE TABLE IF NOT EXISTS "InviteTokens" (
    "Id" UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    "CreatedByUserId" UUID NOT NULL REFERENCES "Users"("Id") ON DELETE CASCADE,
    "Token" VARCHAR(20) NOT NULL UNIQUE,
    "IsUsed" BOOLEAN NOT NULL DEFAULT FALSE,
    "ExpiresAt" TIMESTAMPTZ NOT NULL,
    "CreatedAt" TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    "UsedAt" TIMESTAMPTZ
);

-- Exercises Library
CREATE TABLE IF NOT EXISTS "Exercises" (
    "Id" UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    "Name" VARCHAR(100) NOT NULL UNIQUE,
    "Description" TEXT,
    "MuscleGroup" VARCHAR(100) NOT NULL,
    "Category" VARCHAR(100) NOT NULL,
    "IsBodyweight" BOOLEAN NOT NULL DEFAULT FALSE,
    "RequiresEquipment" BOOLEAN NOT NULL DEFAULT FALSE,
    "CreatedAt" TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Workouts
CREATE TABLE IF NOT EXISTS "Workouts" (
    "Id" UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    "UserId" UUID NOT NULL REFERENCES "Users"("Id") ON DELETE CASCADE,
    "Name" VARCHAR(200) NOT NULL,
    "Notes" TEXT,
    "Status" INTEGER NOT NULL DEFAULT 0,
    "StartedAt" TIMESTAMPTZ NOT NULL,
    "CompletedAt" TIMESTAMPTZ,
    "Duration" INTERVAL,
    "CaloriesBurned" INTEGER,
    "TotalVolume" INTEGER NOT NULL DEFAULT 0,
    "IsShared" BOOLEAN NOT NULL DEFAULT FALSE,
    "SharedWithUserId" UUID,
    "OriginalWorkoutId" UUID,
    "CreatedAt" TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    "UpdatedAt" TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Workout Sessions (for partner joined workouts)
CREATE TABLE IF NOT EXISTS "WorkoutSessions" (
    "Id" UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    "WorkoutId" UUID NOT NULL REFERENCES "Workouts"("Id") ON DELETE CASCADE,
    "UserId" UUID NOT NULL REFERENCES "Users"("Id") ON DELETE CASCADE,
    "PartnerId" UUID NOT NULL,
    "JoinedAt" TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    "IsActive" BOOLEAN NOT NULL DEFAULT TRUE
);

-- Workout-Exercise junction
CREATE TABLE IF NOT EXISTS "WorkoutExercises" (
    "Id" UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    "WorkoutId" UUID NOT NULL REFERENCES "Workouts"("Id") ON DELETE CASCADE,
    "ExerciseId" UUID NOT NULL REFERENCES "Exercises"("Id") ON DELETE CASCADE,
    "Order" INTEGER NOT NULL,
    "Notes" TEXT,
    "CreatedAt" TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Exercise Sets
CREATE TABLE IF NOT EXISTS "ExerciseSets" (
    "Id" UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    "WorkoutExerciseId" UUID NOT NULL REFERENCES "WorkoutExercises"("Id") ON DELETE CASCADE,
    "SetNumber" INTEGER NOT NULL,
    "Reps" INTEGER,
    "Weight" DECIMAL(8,2),
    "DistanceMeters" INTEGER,
    "DurationSeconds" INTEGER,
    "Rpe" INTEGER,
    "Notes" TEXT,
    "IsPersonalRecord" BOOLEAN NOT NULL DEFAULT FALSE,
    "IsCompleted" BOOLEAN NOT NULL DEFAULT FALSE,
    "CreatedAt" TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Rest Timers
CREATE TABLE IF NOT EXISTS "RestTimers" (
    "Id" UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    "WorkoutId" UUID NOT NULL REFERENCES "Workouts"("Id") ON DELETE CASCADE,
    "UserId" UUID NOT NULL,
    "DurationSeconds" INTEGER NOT NULL,
    "RemainingSeconds" INTEGER NOT NULL,
    "IsRunning" BOOLEAN NOT NULL DEFAULT FALSE,
    "StartedAt" TIMESTAMPTZ,
    "CreatedAt" TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Chat Messages
CREATE TABLE IF NOT EXISTS "ChatMessages" (
    "Id" UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    "SenderId" UUID NOT NULL REFERENCES "Users"("Id") ON DELETE CASCADE,
    "ReceiverId" UUID NOT NULL REFERENCES "Users"("Id") ON DELETE CASCADE,
    "TextContent" TEXT,
    "ImageUrl" TEXT,
    "VoiceNoteUrl" TEXT,
    "VoiceNoteDurationSeconds" INTEGER,
    "IsRead" BOOLEAN NOT NULL DEFAULT FALSE,
    "IsDelivered" BOOLEAN NOT NULL DEFAULT FALSE,
    "SentAt" TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    "DeliveredAt" TIMESTAMPTZ,
    "ReadAt" TIMESTAMPTZ
);

-- Motivations
CREATE TABLE IF NOT EXISTS "Motivations" (
    "Id" UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    "SenderId" UUID NOT NULL REFERENCES "Users"("Id") ON DELETE CASCADE,
    "ReceiverId" UUID NOT NULL REFERENCES "Users"("Id") ON DELETE CASCADE,
    "Type" INTEGER NOT NULL,
    "CustomMessage" TEXT,
    "SentAt" TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Challenges
CREATE TABLE IF NOT EXISTS "Challenges" (
    "Id" UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    "Type" INTEGER NOT NULL,
    "Name" VARCHAR(200) NOT NULL,
    "Description" TEXT,
    "Goal" VARCHAR(500) NOT NULL,
    "StartsAt" TIMESTAMPTZ NOT NULL,
    "EndsAt" TIMESTAMPTZ NOT NULL,
    "IsActive" BOOLEAN NOT NULL DEFAULT TRUE,
    "WinnerId" UUID REFERENCES "Users"("Id") ON DELETE SET NULL,
    "CreatedAt" TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Challenge Progress
CREATE TABLE IF NOT EXISTS "ChallengeProgresses" (
    "Id" UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    "ChallengeId" UUID NOT NULL REFERENCES "Challenges"("Id") ON DELETE CASCADE,
    "UserId" UUID NOT NULL REFERENCES "Users"("Id") ON DELETE CASCADE,
    "Progress" DECIMAL(12,2) NOT NULL DEFAULT 0,
    "UpdatedAt" TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE("ChallengeId", "UserId")
);

-- Workout Templates
CREATE TABLE IF NOT EXISTS "WorkoutTemplates" (
    "Id" UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    "UserId" UUID NOT NULL REFERENCES "Users"("Id") ON DELETE CASCADE,
    "Name" VARCHAR(200) NOT NULL,
    "Description" TEXT,
    "IsAdminCreated" BOOLEAN NOT NULL DEFAULT FALSE,
    "CreatedAt" TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    "UpdatedAt" TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Template Exercises
CREATE TABLE IF NOT EXISTS "WorkoutTemplateExercises" (
    "Id" UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    "WorkoutTemplateId" UUID NOT NULL REFERENCES "WorkoutTemplates"("Id") ON DELETE CASCADE,
    "ExerciseId" UUID NOT NULL REFERENCES "Exercises"("Id") ON DELETE CASCADE,
    "Order" INTEGER NOT NULL,
    "DefaultSets" INTEGER NOT NULL DEFAULT 3,
    "DefaultReps" INTEGER NOT NULL DEFAULT 10,
    "DefaultWeight" DECIMAL(8,2),
    "DefaultRestSeconds" INTEGER
);

-- Notifications
CREATE TABLE IF NOT EXISTS "Notifications" (
    "Id" UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    "UserId" UUID NOT NULL REFERENCES "Users"("Id") ON DELETE CASCADE,
    "Title" VARCHAR(200) NOT NULL,
    "Body" VARCHAR(1000) NOT NULL,
    "Data" TEXT,
    "IsRead" BOOLEAN NOT NULL DEFAULT FALSE,
    "CreatedAt" TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    "ReadAt" TIMESTAMPTZ
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS "IX_RefreshTokens_UserId" ON "RefreshTokens"("UserId");
CREATE INDEX IF NOT EXISTS "IX_RefreshTokens_Token" ON "RefreshTokens"("Token");
CREATE INDEX IF NOT EXISTS "IX_Workouts_UserId" ON "Workouts"("UserId");
CREATE INDEX IF NOT EXISTS "IX_Workouts_StartedAt" ON "Workouts"("StartedAt");
CREATE INDEX IF NOT EXISTS "IX_ExerciseSets_WorkoutExerciseId" ON "ExerciseSets"("WorkoutExerciseId");
CREATE INDEX IF NOT EXISTS "IX_ChatMessages_SenderId" ON "ChatMessages"("SenderId");
CREATE INDEX IF NOT EXISTS "IX_ChatMessages_ReceiverId" ON "ChatMessages"("ReceiverId");
CREATE INDEX IF NOT EXISTS "IX_ChatMessages_SentAt" ON "ChatMessages"("SentAt" DESC);
CREATE INDEX IF NOT EXISTS "IX_Notifications_UserId" ON "Notifications"("UserId");
CREATE INDEX IF NOT EXISTS "IX_Notifications_CreatedAt" ON "Notifications"("CreatedAt" DESC);
CREATE INDEX IF NOT EXISTS "IX_ChallengeProgresses_UserId" ON "ChallengeProgresses"("UserId");
