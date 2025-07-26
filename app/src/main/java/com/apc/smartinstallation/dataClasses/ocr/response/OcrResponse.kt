package com.apc.smartinstallation.dataClasses.ocr.response

import com.apc.smartinstallation.dataClasses.ocr.request.OcrRequest

data class OcrResponse(
    var acc_id: String,
    val agency_req_id: String,
    var field_exp: String,
    var meter_status: String,
    var meter_make: String,
    var meter_no: String,
    var ocr_npr: String,
    var read_type:String,
    var ocr_results: List<OcrResult>,
    var probe_npr: String,
    var mr_sr_no:String,
    var input: OcrRequest,
    var address:String,
    var lat:String,
    var lng:String

)