package qr

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach

/**
 * Tests for vCard processing functionality
 * Task 75: Add support for vCard format in QR codes
 */
class VCardProcessorTest {
    
    private lateinit var processor: VCardProcessor
    
    @BeforeEach
    fun setUp() {
        processor = VCardProcessor
    }
    
    @Test
    fun `should detect vCard content correctly`() {
        val vCardContent = """
            BEGIN:VCARD
            VERSION:3.0
            FN:John Doe
            END:VCARD
        """.trimIndent()
        
        assertTrue(processor.isVCard(vCardContent))
        assertFalse(processor.isVCard("Not a vCard"))
        assertFalse(processor.isVCard("BEGIN:VCALENDAR"))
    }
    
    @Test
    fun `should parse simple vCard correctly`() {
        val vCardContent = """
            BEGIN:VCARD
            VERSION:3.0
            FN:John Doe
            N:Doe;John;;;
            ORG:Example Corp
            TITLE:Software Engineer
            TEL;TYPE=WORK,VOICE:+1-555-123-4567
            EMAIL;TYPE=WORK:john.doe@example.com
            END:VCARD
        """.trimIndent()
        
        val vCard = processor.parseVCard(vCardContent)
        
        assertNotNull(vCard)
        assertEquals("John Doe", vCard!!.formattedName)
        assertEquals("John", vCard.firstName)
        assertEquals("Doe", vCard.lastName)
        assertEquals("Example Corp", vCard.organization)
        assertEquals("Software Engineer", vCard.title)
        assertEquals(1, vCard.phoneNumbers.size)
        assertEquals("+1-555-123-4567", vCard.phoneNumbers[0].number)
        assertEquals(VCardProcessor.VCard.PhoneType.WORK, vCard.phoneNumbers[0].type)
        assertEquals(1, vCard.emails.size)
        assertEquals("john.doe@example.com", vCard.emails[0].address)
    }
    
    @Test
    fun `should generate vCard content correctly`() {
        val vCard = VCardProcessor.VCard(
            formattedName = "Jane Smith",
            firstName = "Jane",
            lastName = "Smith",
            organization = "Tech Inc",
            phoneNumbers = listOf(
                VCardProcessor.VCard.PhoneNumber("+1-555-987-6543", VCardProcessor.VCard.PhoneType.CELL)
            ),
            emails = listOf(
                VCardProcessor.VCard.Email("jane@tech.com", VCardProcessor.VCard.EmailType.WORK)
            )
        )
        
        val generated = processor.generateVCard(vCard)
        
        assertTrue(generated.contains("BEGIN:VCARD"))
        assertTrue(generated.contains("END:VCARD"))
        assertTrue(generated.contains("FN:Jane Smith"))
        assertTrue(generated.contains("N:Smith;Jane;;;"))
        assertTrue(generated.contains("ORG:Tech Inc"))
        assertTrue(generated.contains("TEL;TYPE=CELL:+1-555-987-6543"))
        assertTrue(generated.contains("EMAIL;TYPE=WORK:jane@tech.com"))
    }
    
    @Test
    fun `should format vCard for display correctly`() {
        val vCard = VCardProcessor.VCard(
            formattedName = "Test User",
            organization = "Test Org",
            phoneNumbers = listOf(
                VCardProcessor.VCard.PhoneNumber("123-456-7890", VCardProcessor.VCard.PhoneType.HOME)
            )
        )
        
        val formatted = processor.formatVCardForDisplay(vCard)
        
        assertTrue(formatted.contains("Contact Information"))
        assertTrue(formatted.contains("Test User"))
        assertTrue(formatted.contains("Test Org"))
        assertTrue(formatted.contains("123-456-7890"))
    }
}
