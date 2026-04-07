package ru.cards.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.cards.dto.PurchaseSimulationRecord;
import ru.cards.dto.SimulationRunResponse;
import ru.cards.models.Purchase;
import ru.cards.simulation.SimulationRunner;
import ru.cards.simulation.SimulationSessionResult;

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
                    return new PurchaseSimulationRecord(
                            p.getId(),
                            p.getInitialAmount(),
                            p.getRemainingAmount(),
                            p.getStatus(),
                            pr.getResultKind(),
                            SimulationRunner.formatStepsAsText(pr.getCurrentSimulationSteps())
                    );
                })
                .toList();
        return new SimulationRunResponse(
                SimulationRunner.snapshotWallet(session.wallet()),
                purchases);
    }
}
