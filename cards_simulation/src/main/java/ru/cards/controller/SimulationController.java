package ru.cards.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.cards.dto.PurchaseSimulationRecord;
import ru.cards.dto.SimStepDTO;
import ru.cards.dto.SimulationRunResponse;
import ru.cards.models.Purchase;
import ru.cards.simulation.SimulationRunner;
import ru.cards.simulation.SimulationSessionResult;
import ru.cards.simulation.SimulationStep;

import java.util.List;

@RestController
@RequestMapping("/api/simulation")
public class SimulationController {

    @PostMapping("/run")
    public SimulationRunResponse run() {
        SimulationSessionResult session = SimulationRunner.runSimulation();
        List<PurchaseSimulationRecord> purchases = session.purchases().stream()
                .map(pr -> {
                    Purchase p = pr.getPurchase();
                    var steps = pr.getCurrentSimulationSteps().stream()
                            .map(SimulationController::getSimStepDTO)
                            .toList();
                    return new PurchaseSimulationRecord(
                            p.getId(),
                            p.getInitialAmount(),
                            p.getRemainingAmount(),
                            p.getStatus(),
                            pr.getResultKind(),
                            steps
                    );
                })
                .toList();
        return new SimulationRunResponse(
                session.initialCards(),
                SimulationRunner.snapshotWallet(session.wallet()),
                purchases);
    }

    private static SimStepDTO getSimStepDTO(SimulationStep step) {
        return new SimStepDTO(step.card().getClass().getSimpleName(),
                step.card().getCardNumber(),
                step.result(),
                step.sum()
        );
    }
}
