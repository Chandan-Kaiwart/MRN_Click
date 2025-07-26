package com.apc.smartinstallation.dataClasses.ocr.request


data class OcrRequest(
    val AGENCY_ID: String,
    val BOARD_CDOE: String,
    val CONSUMER_NO: String,
    val METER_READER_ID: String,
    val METER_READER_MOBILE_NO: String,
    val METER_READER_NAME: String,
    val SUB_DIVISION_CODE: String,
    val SUB_DIVISON_NAME: String,
    val REQ_READING_VALUES: List<String>,

    )