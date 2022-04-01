/*
Copyright (c) 2012, Apps4Av Inc. (apps4av.com) 
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
    *     * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
    *
    *     THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package com.ds.avare.weather;

/**
 * The weather stations
 * @author zkhan
 *
 */
public class Stations {

    private static final String mStations[] = {       
        "BHM","33.55","-86.73333",
        "HSV","34.55","-86.76667",
        "MGM","32.216667","-86.316666",
        "MOB","30.683332","-88.23333",
        "ADK","51.933334","-176.41667",
        "ADQ","57.766666","-152.58333",
        "AKN","58.733334","-156.75",
        "ANC","61.233334","-149.55",
        "ANN","55.05","-131.61667",
        "BET","60.583332","-161.58333",
        "BRW","71.28333","-156.51666",
        "BTI","70.166664","-143.91667",
        "BTT","66.9","-151.5",
        "CDB","55.183334","-162.36667",
        "CZF","61.783333","-166.03334",
        "EHM","58.65","-162.06667",
        "FAI","64.71667","-148.18333",
        "FYU","66.583336","-145.08333",
        "GAL","64.73333","-156.93333",
        "GKN","62.15","-145.45",
        "HOM","59.65","-151.48334",
        "JNU","58.433334","-134.68333",
        "LUR","68.88333","-166.11667",
        "MCG","62.816666","-155.4",
        "MDO","59.5","-146.3",
        "OME","64.61667","-165.08333",
        "ORT","63.066666","-142.06667",
        "OTZ","66.65","-162.9",
        "SNP","57.15","-170.61667",
        "TKA","62.316666","-150.1",
        "UNK","63.883335","-160.8",
        "YAK","59.616665","-139.5",
        "IKO","52.95","-168.85",
        "AFM","67.1","-157.85",
        "5AB","52.416668","176.0",
        "5AC","52.0","-135.0",
        "5AD","54.0","-145.0",
        "5AE","55.0","-155.0",
        "5AF","56.0","-137.0",
        "5AG","58.0","-142.0",
        "FSM","35.383335","-94.26667",
        "LIT","34.666668","-92.166664",
        "PHX","33.416668","-111.88333",
        "PRC","34.7","-112.46667",
        "TUS","32.116665","-110.816666",
        "BIH","37.366665","-118.35",
        "BLH","33.583332","-114.75",
        "FAT","36.883335","-119.8",
        "FOT","40.666668","-124.23333",
        "ONT","34.05","-117.6",
        "RBL","40.083332","-122.23333",
        "SAC","38.433334","-121.55",
        "SAN","32.733334","-117.183334",
        "SBA","34.5","-119.76667",
        "SFO","37.616665","-122.36667",
        "SIY","41.783333","-122.45",
        "WJF","34.733334","-118.21667",
        "ALS","37.333332","-105.8",
        "DEN","39.8","-104.88333",
        "GJT","39.05","-108.78333",
        "PUB","38.283333","-104.416664",
        "BDL","41.933334","-72.683334",
        "EYW","24.583334","-81.8",
        "JAX","30.433332","-81.55",
        "MIA","25.95","-80.45",
        "MLB","28.1","-80.63333",
        "PFN","30.2","-85.666664",
        "PIE","27.9","-82.683334",
        "TLH","30.55","-84.36667",
        "ATL","33.616665","-84.433334",
        "CSG","32.6","-85.01667",
        "SAV","32.15","-81.1",
        "ITO","19.716667","-155.05",
        "HNL","21.316668","-157.91667",
        "LIH","21.983334","-159.33333",
        "OGG","20.9","-156.43333",
        "LNY","20.783333","-156.95",
        "KOA","19.733334","-156.05",
        "BOI","43.566666","-116.23333",
        "LWS","46.366665","-116.86667",
        "PIH","42.866665","-112.65",
        "JOT","41.533333","-88.316666",
        "SPI","39.833332","-89.666664",
        "EVV","38.033333","-87.51667",
        "FWA","40.966667","-85.183334",
        "IND","39.8","-86.36667",
        "BRL","40.716667","-90.916664",
        "DBQ","42.4","-90.7",
        "DSM","41.433334","-93.63333",
        "MCW","43.083332","-93.316666",
        "GCK","37.916668","-100.71667",
        "GLD","39.383335","-101.683334",
        "ICT","37.716667","-97.45",
        "SLN","38.866665","-97.61667",
        "LOU","38.1","-85.566666",
        "LCH","30.133333","-93.1",
        "MSY","30.016666","-90.166664",
        "SHV","32.766666","-93.8",
        "BGR","44.833332","-68.86667",
        "CAR","46.866665","-68.01667",
        "PWM","43.633335","-70.3",
        "EMI","39.483334","-76.96667",
        "ACK","41.266666","-70.01667",
        "BOS","42.35","-70.98333",
        "ECK","43.25","-82.71667",
        "MKG","43.166668","-86.03333",
        "MQT","46.516666","-87.583336",
        "SSM","46.4","-84.3",
        "TVC","44.666668","-85.53333",
        "AXN","45.95","-95.21667",
        "DLH","46.8","-92.2",
        "INL","48.55","-93.4",
        "MSP","45.133335","-93.36667",
        "CGI","37.216667","-89.566666",
        "COU","38.8","-92.21667",
        "MKC","39.266666","-94.583336",
        "SGF","37.35","-93.333336",
        "STL","38.85","-90.46667",
        "JAN","32.5","-90.166664",
        "BIL","45.8","-108.61667",
        "DLN","45.233334","-112.53333",
        "GPI","48.2","-114.166664",
        "GGW","48.2","-106.61667",
        "GTF","47.45","-111.4",
        "MLS","46.366665","-105.95",
        "HAT","35.266666","-75.55",
        "ILM","34.35","-77.86667",
        "RDU","35.866665","-78.78333",
        "DIK","46.85","-102.76667",
        "GFK","47.95","-97.183334",
        "MOT","48.25","-101.28333",
        "BFF","41.883335","-103.46667",
        "GRI","40.983334","-98.3",
        "OMA","41.166668","-95.73333",
        "ONL","42.466667","-98.683334",
        "BML","44.633335","-71.183334",
        "ACY","39.45","-74.566666",
        "ABQ","35.033333","-106.8",
        "FMN","36.733334","-108.083336",
        "ROW","33.333332","-104.61667",
        "TCC","35.166668","-103.583336",
        "ZUN","34.95","-109.15",
        "BAM","40.566666","-116.916664",
        "ELY","39.283333","-114.833336",
        "LAS","36.066666","-115.15",
        "RNO","39.516666","-119.65",
        "ALB","42.733334","-73.8",
        "BUF","42.916668","-78.63333",
        "JFK","40.616665","-73.76667",
        "PLB","44.8","-73.4",
        "SYR","43.15","-76.2",
        "CLE","41.35","-82.15",
        "CMH","39.983334","-82.916664",
        "CVG","39.0","-84.7",
        "GAG","36.333332","-99.86667",
        "OKC","35.4","-97.63333",
        "TUL","36.183334","-95.78333",
        "AST","46.15","-123.86667",
        "IMB","44.633335","-119.7",
        "LKV","42.483334","-120.5",
        "OTH","43.4","-124.166664",
        "PDX","45.733334","-122.583336",
        "RDM","44.25","-121.3",
        "AGC","40.266666","-80.03333",
        "AVP","41.266666","-75.683334",
        "PSB","40.9","-77.98333",
        "CAE","33.85","-81.05",
        "CHS","32.883335","-80.03333",
        "FLO","34.216667","-79.65",
        "GSP","34.883335","-82.21667",
        "ABR","45.416668","-98.36667",
        "FSD","43.633335","-96.76667",
        "PIR","44.383335","-100.15",
        "RAP","43.966667","-103.0",
        "BNA","36.116665","-86.666664",
        "MEM","35.05","-89.96667",
        "TRI","36.466667","-82.4",
        "TYS","35.9","-83.88333",
        "ABI","32.466667","-99.85",
        "AMA","35.283333","-101.63333",
        "BRO","25.916666","-97.36667",
        "CLL","30.6","-96.416664",
        "CRP","27.9","-97.433334",
        "DAL","32.833332","-96.85",
        "DRT","29.366667","-100.916664",
        "ELP","31.8","-106.26667",
        "HOU","29.633333","-95.26667",
        "INK","31.866667","-103.23333",
        "LBB","33.7","-101.9",
        "LRD","27.466667","-99.416664",
        "MRF","30.283333","-103.61667",
        "PSX","28.75","-96.3",
        "SAT","28.633333","-98.45",
        "SPS","33.983334","-98.583336",
        "BCE","37.683334","-112.3",
        "SLC","40.85","-111.96667",
        "ORF","36.883335","-76.2",
        "RIC","37.5","-77.316666",
        "ROA","37.333332","-80.066666",
        "GEG","47.55","-117.61667",
        "SEA","47.433334","-122.3",
        "YKM","46.566666","-120.433334",
        "GRB","44.55","-88.183334",
        "LSE","43.866665","-91.25",
        "CRW","38.333332","-81.76667",
        "EKN","38.9","-80.083336",
        "CZI","43.983334","-106.433334",
        "LND","42.8","-108.71667",
        "MBW","41.833332","-106.0",
        "RKS","41.583332","-109.0",
        "2XG","30.333334","-78.5",
        "T01","28.5","-93.5",
        "T06","28.5","-91.0",
        "T07","28.5","-88.0",
        "4J3","28.5","-85.0",
        "H51","26.5","-95.0",
        "H52","26.0","-89.5",
        "H61","26.5","-84.0",
        "JON","16.733334","-169.53334",
        "MAJ","7.0666666","171.26666",
        "KWA","8.716666","167.73334",
        "MDY","28.2","-177.38333",
        "PPG","-14.333333","-170.71666",
        "TTK","5.35","162.96666",
        "AWK","19.283333","166.65",
        "GRO","14.183333","145.23334",
        "GSN","15.116667","145.73334",
        "TNI","15.0","145.61667",
        "GUM","13.483334","144.8",
        "TKK","7.4666667","151.85",
        "PNI","6.9833336","158.21666",
        "ROR","7.366667","134.55",
        "T11","9.5","138.08333"
    };

    /**
     * Get station coordinates
     */
    public static boolean getStationLocation(String station, float coords[]) {
        for(int i = 0; i < mStations.length; i+= 3) {
            if(station.equals(mStations[i])) {
                coords[0] = Float.parseFloat(mStations[i + 2]);
                coords[1] = Float.parseFloat(mStations[i + 1]);
                return true;
            }
        }
        
        return false;
    }
}
