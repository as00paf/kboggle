package org.pafoid.kboggle.extensions

import java.text.Normalizer

fun String.removeAccents(): String {
    val normalized = Normalizer.normalize(this, Normalizer.Form.NFD)
    return normalized.replace("\\p{InCombiningDiacriticalMarks}".toRegex(), "")
}