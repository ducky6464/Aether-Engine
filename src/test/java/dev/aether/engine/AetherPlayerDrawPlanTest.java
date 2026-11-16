package dev.aether.engine;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AetherPlayerDrawPlanTest {
 @Test void buildsOnlyValidGeometryPacketsAndPreservesSkinGroups(){
  String[] skins={"skin-a","skin-b","skin-a","skin-c"};AetherGroupIndex<String> groups=new AetherGroupIndex<>(4);groups.build(skins,4);
  AetherPlayerBatchPlan batches=new AetherPlayerBatchPlan(4);batches.build(groups,new int[4],4);
  int[] offsets={0,60,-1,120},floats={60,30,0,60},vertices={6,3,0,6};
  AetherPlayerGeometryDrawPlan plan=new AetherPlayerGeometryDrawPlan(4);plan.build(batches,offsets,floats,vertices,4);
  assertEquals(3,plan.packetCount());assertEquals(3,plan.batchCount());assertEquals(1,plan.missingGeometryCount());assertEquals(0,plan.rejectedRangeCount());
  assertEquals(0,plan.packetPlayer(0,0));assertEquals(0,plan.firstVertex(0,0));assertEquals(6,plan.vertexCount(0,0));
  assertEquals(1,plan.packetPlayer(1,0));assertEquals(6,plan.firstVertex(1,0));assertEquals(3,plan.vertexCount(1,0));
  assertEquals(3,plan.packetPlayer(2,0));assertEquals(12,plan.firstVertex(2,0));
 }
 @Test void rejectsMisalignedAndInconsistentGeometryRanges(){
  String[] skins={"skin-a","skin-b","skin-c"};AetherGroupIndex<String> groups=new AetherGroupIndex<>(3);groups.build(skins,3);
  AetherPlayerBatchPlan batches=new AetherPlayerBatchPlan(3);batches.build(groups,new int[3],3);
  AetherPlayerGeometryDrawPlan plan=new AetherPlayerGeometryDrawPlan(3);
  plan.build(batches,new int[]{1,28,60},new int[]{60,25,30},new int[]{6,3,2},3);
  assertEquals(0,plan.packetCount());assertEquals(3,plan.rejectedRangeCount());
 }
 @Test void rejectsMismatchedPlayerCounts(){
  AetherGroupIndex<String> groups=new AetherGroupIndex<>(2);groups.build(new String[]{"skin-a"},1);
  AetherPlayerBatchPlan batches=new AetherPlayerBatchPlan(2);batches.build(groups,new int[2],1);
  AetherPlayerGeometryDrawPlan plan=new AetherPlayerGeometryDrawPlan(2);
  assertThrows(IllegalArgumentException.class,()->plan.build(batches,new int[2],new int[2],new int[2],2));
 }
 @Test void clearsPreviousPacketsAndCountersWhenRebuiltWithNoPlayers(){
  AetherGroupIndex<String> groups=new AetherGroupIndex<>(2);groups.build(new String[]{"skin-a","skin-b"},2);
  AetherPlayerBatchPlan batches=new AetherPlayerBatchPlan(2);batches.build(groups,new int[2],2);
  AetherPlayerGeometryDrawPlan plan=new AetherPlayerGeometryDrawPlan(2);
  plan.build(batches,new int[]{0,-1},new int[]{60,0},new int[]{6,0},2);
  assertEquals(1,plan.packetCount());assertEquals(1,plan.missingGeometryCount());
  groups.clear();batches.build(groups,new int[2],0);plan.build(batches,new int[0],new int[0],new int[0],0);
  assertEquals(0,plan.packetCount());assertEquals(0,plan.batchCount());assertEquals(0,plan.missingGeometryCount());assertEquals(0,plan.rejectedRangeCount());
 }
}
