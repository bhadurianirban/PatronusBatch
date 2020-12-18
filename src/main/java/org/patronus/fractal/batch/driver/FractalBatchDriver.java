/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.patronus.fractal.batch.driver;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hedwig.cloud.client.TenantListClient;
import org.hedwig.cloud.dto.HedwigAuthCredentials;
import org.hedwig.cloud.dto.TenantDTO;
import org.patronus.fractal.batch.psvg.FractalCalc;
import org.patronus.constants.PatronusConstants;

/**
 *
 * @author dgrfi
 */
public class FractalBatchDriver {
    private HedwigAuthCredentials authCredentials;
    private static final int PRODUCT_ID = 3;

    public FractalBatchDriver() {
        authCredentials = new HedwigAuthCredentials();
        authCredentials.setProductId(PRODUCT_ID);
        authCredentials.setHedwigServer("localhost");
        authCredentials.setHedwigServerPort("8080");
    }
    
    public void getTenantList() {
        CMSClientAuthCredentialValue.AUTH_CREDENTIALS = authCredentials;
        TenantListClient tenantListClient = new TenantListClient(CMSClientAuthCredentialValue.AUTH_CREDENTIALS.getHedwigServer(),CMSClientAuthCredentialValue.AUTH_CREDENTIALS.getHedwigServerPort());
        List<TenantDTO> tenantDTOs = tenantListClient.getTenantList(PRODUCT_ID);
        for (TenantDTO tenantDTO: tenantDTOs) {
            CMSClientAuthCredentialValue.AUTH_CREDENTIALS.setTenantId(tenantDTO.getTenantId());
            Logger.getLogger(FractalBatchDriver.class.getName()).log(Level.INFO, "Emptying queue for tenant{0}", tenantDTO.getName());
            emtyQueuedCalculations();
        }
    }
    public void emtyQueuedCalculations() {
        FractalCalc psvgfc = new FractalCalc(PatronusConstants.TERM_SLUG_PSVG_CALC);
        psvgfc.emptyQueueForATenant();
        FractalCalc mfdfafc = new FractalCalc(PatronusConstants.TERM_SLUG_MFDFA_CALC);
        mfdfafc.emptyQueueForATenant();
        FractalCalc mfdxafc = new FractalCalc(PatronusConstants.TERM_SLUG_MFDXA_CALC);
        mfdxafc.emptyQueueForATenant();
        FractalCalc ipsvgfc = new FractalCalc(PatronusConstants.TERM_SLUG_IPSVG_CALC);
        ipsvgfc.emptyQueueForATenant();
        
    }
    
}
