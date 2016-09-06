package cz.agents.alite.protobuf;

import java.util.Collection;

import cz.agents.highway.storage.plan.Action;

public interface PlanCallback {
      void uploadPlan(Collection<Action> plan);
}