/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zygon.rl.data_structures;

import com.zygon.rl.data_structures.QuestImpl.QuestContext;

import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author zygon
 */
public class QuestTester {

    private static final class QuestContextImpl implements QuestContext {

        private final boolean isComplete;
        private final boolean isSuccess;

        private QuestContextImpl(boolean isComplete, boolean isSuccess) {
            this.isComplete = isComplete;
            this.isSuccess = isSuccess;
        }

        @Override
        public boolean isComplete() {
            return isComplete;
        }

        @Override
        public boolean isSuccess() {
            return isSuccess;
        }

        public static QuestContext create(boolean isComplete, boolean isSuccess) {
            return new QuestContextImpl(isComplete, isSuccess);
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        QuestImpl gatherQuest = new QuestImpl("Gather", "Gather the ingredients for the summoning ritual",
                QuestContextImpl.create(true, true));

        QuestImpl summonQuest = new QuestImpl("Summon", "Perform the summoning ritual",
                QuestContextImpl.create(true, true),
                List.of(gatherQuest));

        QuestImpl weakenQuest = new QuestImpl("Weaken", "Perform the weakening ritual",
                QuestContextImpl.create(true, false));

        QuestImpl trapQuest = new QuestImpl("Trap", "Perform the trapping ritual",
                QuestContextImpl.create(false, false));

        QuestImpl deicideQuest = new QuestImpl("Deicide",
                "Perform the fancy ritual to summon and trap the god, and poke it in the eye.",
                QuestContextImpl.create(false, false),
                List.of(summonQuest, weakenQuest, trapQuest));

        System.out.println(deicideQuest);
        System.out.println("Complete? " + deicideQuest.isComplete());
        System.out.println("Successful? " + deicideQuest.isSuccess());

        System.out.println(deicideQuest.getDependentSubTasks().stream()
                .map(TaskInfo::toString)
                .collect(Collectors.joining("\n")));

    }
}
