/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rpax.massis.ai.sposh.actions;

import cz.cuni.amis.pogamut.sposh.executor.ActionResult;
import cz.cuni.amis.pogamut.sposh.executor.PrimitiveInfo;
import rpax.massis.ai.sposh.SimulationContext;

/**
 * Primitive action that doesn't do anything. It is used in empty plans and so on.
 * @author rpax
 */
@PrimitiveInfo(name = "Nothing", description = "This action does nothing and lasts one iteration.")
public class DoNothing extends SimulationAction {

    public DoNothing(SimulationContext ctx) {
        super(ctx);
    }

    
    public ActionResult run() {
        return ActionResult.RUNNING_ONCE;
    }

    @Override
    public void init() {}
    @Override
    public void done() {}
}
