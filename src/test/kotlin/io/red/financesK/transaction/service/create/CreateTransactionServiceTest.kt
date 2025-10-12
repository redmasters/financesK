package io.red.financesK.transaction.service.create

import io.red.financesK.account.enums.AccountTypeEnum
import io.red.financesK.account.model.Account
import io.red.financesK.account.service.search.SearchAccountService
import io.red.financesK.category.model.Category
import io.red.financesK.category.service.search.SearchCategoryService
import io.red.financesK.transaction.enums.RecurrencePattern
import io.red.financesK.transaction.repository.TransactionRepository
import io.red.financesK.user.model.AppUser
import io.red.financesK.user.service.search.SearchUserService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.context.ApplicationEventPublisher
import java.time.LocalDate
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class CreateTransactionServiceTest {

    @Mock
    private lateinit var transactionRepository: TransactionRepository

    @Mock
    private lateinit var searchCategoryService: SearchCategoryService

    @Mock
    private lateinit var searchUserService: SearchUserService

    @Mock
    private lateinit var searchAccountService: SearchAccountService

    @Mock
    private lateinit var applicationEventPublisher: ApplicationEventPublisher

    @InjectMocks
    private lateinit var createTransactionService: CreateTransactionService

    private lateinit var category: Category
    private lateinit var user: AppUser
    private lateinit var account: Account

    @BeforeEach
    fun setUp() {
        category = Category(
            id = 1,
            name = "Test Category",
            icon = "test-icon",
            color = "#FF0000",
            parent = null
        )

        user = AppUser(
            id = 1,
            username = "testuser",
            email = "test@test.com",
            passwordHash = "hashedpassword",
            passwordSalt = "salt",
            pathAvatar = null,
            createdAt = null,
            updatedAt = null
        )

        account = Account(
            accountId = 1,
            accountName = "Test Account",
            accountDescription = null,
            bankInstitution = null,
            accountType = AccountTypeEnum.CONTA_CORRENTE,
            accountCreditLimit = null,
            accountStatementClosingDate = null,
            accountPaymentDueDate = null,
            accountCurrentBalance = 1000,
            accountCurrency = "BRL",
            userId = user,
            createdAt = null,
            updatedAt = null
        )
    }

    @Test
    fun `test transaction date`() {

        val recurrence = RecurrencePattern.MONTHLY
        val dueDate = LocalDate.of(2025, 10, 17)
        val occurrences = createTransactionService.calculateOccurrences(dueDate, recurrence)
        // Convert the range 1..occurrences to a list of integers
        val occurrencesList = (1..occurrences).toList()


        val expectedDates = listOf(
            LocalDate.of(2025, 10, 17),
            LocalDate.of(2025, 11, 17),
            LocalDate.of(2025, 12, 17),
        )
        val actualDates = mutableListOf<LocalDate>()
        for (i in occurrencesList) {
            val date = createTransactionService.calculateTransactionDate(dueDate, recurrence, i)
            actualDates.add(date)
        }

        assertEquals(expectedDates, actualDates)
    }


}

