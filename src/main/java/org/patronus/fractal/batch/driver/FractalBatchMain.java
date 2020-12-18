/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.patronus.fractal.batch.driver;

/**
 *
 * @author bhaduri
 */
public class FractalBatchMain {
    public static void main(String args[]) {
        FractalBatchDriver fbd = new FractalBatchDriver();
        fbd.getTenantList();
        fbd.emtyQueuedCalculations();
    }
}
