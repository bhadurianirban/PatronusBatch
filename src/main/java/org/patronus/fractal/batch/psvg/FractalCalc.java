/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.patronus.fractal.batch.psvg;

import org.patronus.fractal.batch.driver.CMSClientAuthCredentialValue;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.leviosa.core.driver.LeviosaClientService;

import org.hedwig.cms.dto.TermInstanceDTO;
import org.patronus.constants.PatronusConstants;
import org.patronus.core.client.PatronusCoreClient;
import org.patronus.core.dto.FractalDTO;
import org.patronus.response.FractalResponseCode;
import org.patronus.response.FractalResponseMessage;
import org.patronus.termmeta.MFDXAResultsMeta;
import org.patronus.termmeta.PSVGResultsMeta;

/**
 *
 * @author dgrfi
 */
public class FractalCalc {

    private final List<Map<String, Object>> queuedTermInstanceList;
    private String fractalParamSlug;
    private String dataSeriesSlug;
    private String dataSeriesSlugSecond;
    private String calcType;
    private Map<String, Object> fractalCalcTermInstance;
    private final String fractalCalcType;

    public FractalCalc(String fractalCalcType) {
        queuedTermInstanceList = new ArrayList<>();
        this.fractalCalcType = fractalCalcType;
    }

    private void getQueuedList() {
        LeviosaClientService mts = new LeviosaClientService(CMSClientAuthCredentialValue.AUTH_CREDENTIALS.getHedwigServer(),CMSClientAuthCredentialValue.AUTH_CREDENTIALS.getHedwigServerPort());
        TermInstanceDTO termInstanceDTO = new TermInstanceDTO();
        termInstanceDTO.setHedwigAuthCredentials(CMSClientAuthCredentialValue.AUTH_CREDENTIALS);
        termInstanceDTO.setTermSlug(fractalCalcType);
        termInstanceDTO = mts.getTermInstanceList(termInstanceDTO);
        if (termInstanceDTO.getResponseCode() != FractalResponseCode.SUCCESS) {
            FractalResponseMessage responseMessage = new FractalResponseMessage();
            String message = responseMessage.getResponseMessage(termInstanceDTO.getResponseCode());
            if (termInstanceDTO.getResponseCode() == FractalResponseCode.TERM_INSTANCE_NOT_EXISTS) {
                Logger.getLogger(FractalCalc.class.getName()).log(Level.INFO, "No Queue for Fractal Calculation {0}", fractalCalcType);
            } else {
                Logger.getLogger(FractalCalc.class.getName()).log(Level.SEVERE, "Fractal Calculation not done for {0} due to {1}", new Object[]{fractalCalcType, message});
            }
        } else {
            List<Map<String, Object>> psvgTermInstanceList = termInstanceDTO.getTermInstanceList();
            for (Map<String, Object> psvgTermInstance : psvgTermInstanceList) {
                String queued = (String) psvgTermInstance.get(PSVGResultsMeta.QUEUED);
                if (queued.equals("Yes")) {
                    queuedTermInstanceList.add(psvgTermInstance);
                }
            }
        }
    }

    private void getParamAndData() {
        fractalParamSlug = (String) fractalCalcTermInstance.get(PSVGResultsMeta.PSVG_PARAM);
        dataSeriesSlug = (String) fractalCalcTermInstance.get(PSVGResultsMeta.DATASERIES);
        dataSeriesSlugSecond = (String) fractalCalcTermInstance.get(MFDXAResultsMeta.DATASERIES_SECOND);
        calcType = (String) fractalCalcTermInstance.get(PSVGResultsMeta.PSVG_CALC_TYPE);
    }

    private void calcFractal() {
        //populate PSVG results instance
        PatronusCoreClient fractalCoreClient = new PatronusCoreClient();
        FractalDTO fractalDTO = new FractalDTO();
        fractalDTO.setHedwigAuthCredentials(CMSClientAuthCredentialValue.AUTH_CREDENTIALS);
        fractalDTO.setParamSlug(fractalParamSlug);
        fractalDTO.setDataSeriesSlug(dataSeriesSlug);
        fractalDTO.setDataSeriesSlugSecond(dataSeriesSlugSecond);
        fractalDTO.setCalcType(calcType);
        Logger.getLogger(FractalCalc.class.getName()).log(Level.INFO, "Processing {0} for {1} with parameter {2}", new Object[]{fractalCalcType, dataSeriesSlug, fractalParamSlug});
        switch (fractalCalcType) {
            case PatronusConstants.TERM_SLUG_PSVG_CALC:
                
                fractalDTO = fractalCoreClient.calculatePSVG(fractalDTO);
                break;
            case PatronusConstants.TERM_SLUG_MFDFA_CALC:
                fractalDTO = fractalCoreClient.calculateMFDFA(fractalDTO);
                break;
            case PatronusConstants.TERM_SLUG_MFDXA_CALC:
                fractalDTO = fractalCoreClient.calculateMFDXA(fractalDTO);
                break;
            case PatronusConstants.TERM_SLUG_IPSVG_CALC:
                fractalDTO = fractalCoreClient.calculateImprovedPSVG(fractalDTO);
                break;
            default:
                System.out.println("Invalid Calculation type.");
        }

        if (fractalDTO.getResponseCode() != FractalResponseCode.SUCCESS) {
            FractalResponseMessage responseMessage = new FractalResponseMessage();
            String message = responseMessage.getResponseMessage(fractalDTO.getResponseCode());
            Logger.getLogger(FractalCalc.class.getName()).log(Level.SEVERE, "Problem in Fractal Calculation with parameter {0} for data {1}due to{2}", new Object[]{fractalParamSlug, dataSeriesSlug, message});
        } else {
            Map<String, Object> fractalTermInstance = fractalDTO.getFractalTermInstance();
        }
    }

    public void emptyQueueForATenant() {
        getQueuedList();
        for (Map<String, Object> fractalTermInstance : queuedTermInstanceList) {
            fractalCalcTermInstance = fractalTermInstance;
            getParamAndData();
            calcFractal();
        }
    }
}
