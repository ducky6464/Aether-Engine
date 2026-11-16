package dev.aether.engine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AetherPlayerDrawPlanTest {
    @Test
    void buildsOnlyValidGeometryPacketsAndPreservesSkinGroups() {
        String[] skins = {"skin-a", "skin-b", "skin-a", "skin-c"};
        AetherGroupIndex<String> groups = new AetherGroupIndex<>(4);
        groups.build(skins, skins.length);

        AetherPlayerBatchPlan batches = new AetherPlayerBatchPlan(4);
        batches.build(groups, new int[4], skins.length);

        int[] offsets = {0, 48, -1, 96};
        int[] floatCounts = {48, 24, 0, 48};
        int[] vertexCounts = {6, 3, 0, 6};
        AetherPlayerGeometryDrawPlan plan = new AetherPlayerGeometryDrawPlan(4);
        plan.build(batches, offsets, floatCounts, vertexCounts, skins.length);

        assertEquals(3, plan.packetCount());
        assertEquals(3, plan.batchCount());
        assertEquals(1, plan.missingGeometryCount());
        assertEquals(0, plan.rejectedRangeCount());

        assertEquals(0, plan.packetPlayer(0, 0));
        assertEquals(0, plan.firstVertex(0, 0));
        assertEquals(6, plan.vertexCount(0, 0));
        assertEquals(1, plan.packetPlayer(1, 0));
        assertEquals(12, plan.firstVertex(1, 0));
        assertEquals(3, plan.vertexCount(1, 0));
        assertEquals(3, plan.packetPlayer(2, 0));
    }

    @Test
    void rejectsMisalignedAndInconsistentGeometryRanges() {
        String[] skins = {"skin-a", "skin-b", "skin-c"};
        AetherGroupIndex<String> groups = new AetherGroupIndex<>(3);
        groups.build(skins, skins.length);

        AetherPlayerBatchPlan batches = new AetherPlayerBatchPlan(3);
        batches.build(groups, new int[3], 3);

        int[] offsets = {1, 24, 48};
        int[] floatCounts = {48, 25, 24};
        int[] vertexCounts = {6, 3, 2};
        AetherPlayerGeometryDrawPlan plan = new AetherPlayerGeometryDrawPlan(3);
        plan.build(batches, offsets, floatCounts, vertexCounts, 3);

        assertEquals(0, plan.packetCount());
        assertEquals(3, plan.rejectedRangeCount());
    }

    @Test
    void rejectsMismatchedPlayerCounts() {
        AetherGroupIndex<String> groups = new AetherGroupIndex<>(2);
        groups.build(new String[]{"skin-a"}, 1);
        AetherPlayerBatchPlan batches = new AetherPlayerBatchPlan(2);
        batches.build(groups, new int[2], 1);

        AetherPlayerGeometryDrawPlan plan = new AetherPlayerGeometryDrawPlan(2);
        assertThrows(IllegalArgumentException.class,
                () -> plan.build(batches, new int[2], new int[2], new int[2], 2));
    }
}
