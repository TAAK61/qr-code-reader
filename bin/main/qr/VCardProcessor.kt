package qr

/**
 * vCard parser and generator for QR codes
 * Task 75: Add support for vCard format in QR codes
 */
object VCardProcessor {
    
    data class VCard(
        val formattedName: String? = null,
        val firstName: String? = null,
        val lastName: String? = null,
        val organization: String? = null,
        val title: String? = null,
        val phoneNumbers: List<PhoneNumber> = emptyList(),
        val emails: List<Email> = emptyList(),
        val addresses: List<Address> = emptyList(),
        val url: String? = null,
        val note: String? = null,
        val photo: String? = null
    ) {
        data class PhoneNumber(
            val number: String,
            val type: PhoneType = PhoneType.VOICE
        )
        
        data class Email(
            val address: String,
            val type: EmailType = EmailType.INTERNET
        )
        
        data class Address(
            val street: String? = null,
            val city: String? = null,
            val state: String? = null,
            val postalCode: String? = null,
            val country: String? = null,
            val type: AddressType = AddressType.HOME
        )
        
        enum class PhoneType { VOICE, HOME, WORK, CELL, FAX }
        enum class EmailType { INTERNET, HOME, WORK }
        enum class AddressType { HOME, WORK }
    }
    
    /**
     * Check if content is a vCard
     */
    fun isVCard(content: String): Boolean {
        val trimmed = content.trim()
        return trimmed.startsWith("BEGIN:VCARD", ignoreCase = true) &&
               trimmed.contains("END:VCARD", ignoreCase = true)
    }
    
    /**
     * Parse vCard content from QR code
     */
    fun parseVCard(content: String): VCard? {
        if (!isVCard(content)) return null
        
        val lines = content.lines().map { it.trim() }.filter { it.isNotEmpty() }
        var formattedName: String? = null
        var firstName: String? = null
        var lastName: String? = null
        var organization: String? = null
        var title: String? = null
        val phoneNumbers = mutableListOf<VCard.PhoneNumber>()
        val emails = mutableListOf<VCard.Email>()
        val addresses = mutableListOf<VCard.Address>()
        var url: String? = null
        var note: String? = null
        var photo: String? = null
        
        for (line in lines) {
            when {
                line.startsWith("FN:", ignoreCase = true) -> {
                    formattedName = extractValue(line)
                }
                line.startsWith("N:", ignoreCase = true) -> {
                    val nameParts = extractValue(line)?.split(";") ?: emptyList()
                    if (nameParts.isNotEmpty()) lastName = nameParts[0]
                    if (nameParts.size > 1) firstName = nameParts[1]
                }
                line.startsWith("ORG:", ignoreCase = true) -> {
                    organization = extractValue(line)
                }
                line.startsWith("TITLE:", ignoreCase = true) -> {
                    title = extractValue(line)
                }
                line.startsWith("TEL", ignoreCase = true) -> {
                    val phoneNumber = parsePhoneNumber(line)
                    if (phoneNumber != null) phoneNumbers.add(phoneNumber)
                }
                line.startsWith("EMAIL", ignoreCase = true) -> {
                    val email = parseEmail(line)
                    if (email != null) emails.add(email)
                }
                line.startsWith("ADR", ignoreCase = true) -> {
                    val address = parseAddress(line)
                    if (address != null) addresses.add(address)
                }
                line.startsWith("URL:", ignoreCase = true) -> {
                    url = extractValue(line)
                }
                line.startsWith("NOTE:", ignoreCase = true) -> {
                    note = extractValue(line)
                }
                line.startsWith("PHOTO:", ignoreCase = true) -> {
                    photo = extractValue(line)
                }
            }
        }
        
        return VCard(
            formattedName = formattedName,
            firstName = firstName,
            lastName = lastName,
            organization = organization,
            title = title,
            phoneNumbers = phoneNumbers,
            emails = emails,
            addresses = addresses,
            url = url,
            note = note,
            photo = photo
        )
    }
    
    /**
     * Generate vCard content for QR code
     */
    fun generateVCard(vcard: VCard): String {
        val builder = StringBuilder()
        builder.appendLine("BEGIN:VCARD")
        builder.appendLine("VERSION:3.0")
        
        vcard.formattedName?.let { builder.appendLine("FN:$it") }
        
        if (vcard.firstName != null || vcard.lastName != null) {
            val name = "${vcard.lastName ?: ""};${vcard.firstName ?: ""};;;"
            builder.appendLine("N:$name")
        }
        
        vcard.organization?.let { builder.appendLine("ORG:$it") }
        vcard.title?.let { builder.appendLine("TITLE:$it") }
        
        vcard.phoneNumbers.forEach { phone ->
            val typeStr = when (phone.type) {
                VCard.PhoneType.HOME -> "HOME,VOICE"
                VCard.PhoneType.WORK -> "WORK,VOICE"
                VCard.PhoneType.CELL -> "CELL"
                VCard.PhoneType.FAX -> "FAX"
                VCard.PhoneType.VOICE -> "VOICE"
            }
            builder.appendLine("TEL;TYPE=$typeStr:${phone.number}")
        }
        
        vcard.emails.forEach { email ->
            val typeStr = when (email.type) {
                VCard.EmailType.HOME -> "HOME"
                VCard.EmailType.WORK -> "WORK"
                VCard.EmailType.INTERNET -> "INTERNET"
            }
            builder.appendLine("EMAIL;TYPE=$typeStr:${email.address}")
        }
        
        vcard.addresses.forEach { addr ->
            val typeStr = when (addr.type) {
                VCard.AddressType.HOME -> "HOME"
                VCard.AddressType.WORK -> "WORK"
            }
            val addressStr = ";;${addr.street ?: ""};${addr.city ?: ""};${addr.state ?: ""};${addr.postalCode ?: ""};${addr.country ?: ""}"
            builder.appendLine("ADR;TYPE=$typeStr:$addressStr")
        }
        
        vcard.url?.let { builder.appendLine("URL:$it") }
        vcard.note?.let { builder.appendLine("NOTE:$it") }
        vcard.photo?.let { builder.appendLine("PHOTO:$it") }
        
        builder.appendLine("END:VCARD")
        return builder.toString()
    }
    
    /**
     * Extract value from vCard line
     */
    private fun extractValue(line: String): String? {
        val colonIndex = line.indexOf(':')
        return if (colonIndex != -1 && colonIndex < line.length - 1) {
            line.substring(colonIndex + 1).trim()
        } else null
    }
    
    /**
     * Parse phone number from vCard line
     */
    private fun parsePhoneNumber(line: String): VCard.PhoneNumber? {
        val value = extractValue(line) ?: return null
        val type = when {
            line.contains("HOME", ignoreCase = true) -> VCard.PhoneType.HOME
            line.contains("WORK", ignoreCase = true) -> VCard.PhoneType.WORK
            line.contains("CELL", ignoreCase = true) -> VCard.PhoneType.CELL
            line.contains("FAX", ignoreCase = true) -> VCard.PhoneType.FAX
            else -> VCard.PhoneType.VOICE
        }
        return VCard.PhoneNumber(value, type)
    }
    
    /**
     * Parse email from vCard line
     */
    private fun parseEmail(line: String): VCard.Email? {
        val value = extractValue(line) ?: return null
        val type = when {
            line.contains("HOME", ignoreCase = true) -> VCard.EmailType.HOME
            line.contains("WORK", ignoreCase = true) -> VCard.EmailType.WORK
            else -> VCard.EmailType.INTERNET
        }
        return VCard.Email(value, type)
    }
    
    /**
     * Parse address from vCard line
     */
    private fun parseAddress(line: String): VCard.Address? {
        val value = extractValue(line) ?: return null
        val parts = value.split(";")
        val type = when {
            line.contains("HOME", ignoreCase = true) -> VCard.AddressType.HOME
            line.contains("WORK", ignoreCase = true) -> VCard.AddressType.WORK
            else -> VCard.AddressType.HOME
        }
        
        return VCard.Address(
            street = parts.getOrNull(2)?.takeIf { it.isNotEmpty() },
            city = parts.getOrNull(3)?.takeIf { it.isNotEmpty() },
            state = parts.getOrNull(4)?.takeIf { it.isNotEmpty() },
            postalCode = parts.getOrNull(5)?.takeIf { it.isNotEmpty() },
            country = parts.getOrNull(6)?.takeIf { it.isNotEmpty() },
            type = type
        )
    }
    
    /**
     * Format vCard for human-readable display
     */
    fun formatVCardForDisplay(vcard: VCard): String {
        val builder = StringBuilder()
          builder.appendLine("📇 Contact Information")
        builder.appendLine("=".repeat(30))
        
        vcard.formattedName?.let { builder.appendLine("👤 Name: $it") }
        if (vcard.firstName != null || vcard.lastName != null) {
            val fullName = "${vcard.firstName ?: ""} ${vcard.lastName ?: ""}".trim()
            if (fullName.isNotEmpty() && fullName != vcard.formattedName) {
                builder.appendLine("📝 Full Name: $fullName")
            }
        }
        
        vcard.organization?.let { builder.appendLine("🏢 Organization: $it") }
        vcard.title?.let { builder.appendLine("💼 Title: $it") }
        
        if (vcard.phoneNumbers.isNotEmpty()) {
            builder.appendLine("\n📞 Phone Numbers:")
            vcard.phoneNumbers.forEach { phone ->
                val typeIcon = when (phone.type) {
                    VCard.PhoneType.HOME -> "🏠"
                    VCard.PhoneType.WORK -> "💼"
                    VCard.PhoneType.CELL -> "📱"
                    VCard.PhoneType.FAX -> "📠"
                    VCard.PhoneType.VOICE -> "☎️"
                }
                builder.appendLine("  $typeIcon ${phone.type.name}: ${phone.number}")
            }
        }
        
        if (vcard.emails.isNotEmpty()) {
            builder.appendLine("\n✉️ Email Addresses:")
            vcard.emails.forEach { email ->
                val typeIcon = when (email.type) {
                    VCard.EmailType.HOME -> "🏠"
                    VCard.EmailType.WORK -> "💼"
                    VCard.EmailType.INTERNET -> "🌐"
                }
                builder.appendLine("  $typeIcon ${email.type.name}: ${email.address}")
            }
        }
        
        if (vcard.addresses.isNotEmpty()) {
            builder.appendLine("\n🏠 Addresses:")
            vcard.addresses.forEach { addr ->
                val typeIcon = when (addr.type) {
                    VCard.AddressType.HOME -> "🏠"
                    VCard.AddressType.WORK -> "💼"
                }
                builder.appendLine("  $typeIcon ${addr.type.name}:")
                addr.street?.let { builder.appendLine("    Street: $it") }
                addr.city?.let { builder.appendLine("    City: $it") }
                addr.state?.let { builder.appendLine("    State: $it") }
                addr.postalCode?.let { builder.appendLine("    Postal Code: $it") }
                addr.country?.let { builder.appendLine("    Country: $it") }
            }
        }
        
        vcard.url?.let { builder.appendLine("\n🌐 Website: $it") }
        vcard.note?.let { builder.appendLine("\n📝 Note: $it") }
        
        return builder.toString()
    }
}
