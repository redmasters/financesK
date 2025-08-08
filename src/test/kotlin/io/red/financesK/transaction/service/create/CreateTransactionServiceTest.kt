package io.red.financesK.transaction.service.create

import io.red.financesK.account.balance.service.update.OperationBalanceService
import io.red.financesK.account.model.Account
import io.red.financesK.account.service.search.SearchAccountService
import io.red.financesK.global.exception.ValidationException
import io.red.financesK.transaction.controller.request.CreateTransactionRequest
import io.red.financesK.transaction.enums.PaymentStatus
import io.red.financesK.transaction.enums.TransactionType
import io.red.financesK.transaction.model.Category
import io.red.financesK.transaction.model.Transaction
import io.red.financesK.transaction.repository.CategoryRepository
import io.red.financesK.transaction.repository.TransactionRepository
import io.red.financesK.user.model.AppUser
import io.red.financesK.user.repository.AppUserRepository
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.util.*

@ExtendWith(MockitoExtension::class)
class CreateTransactionServiceTest {

    @Mock
    private lateinit var transactionRepository: TransactionRepository

    @Mock
    private lateinit var categoryRepository: CategoryRepository

    @Mock
    private lateinit var appUserRepository: AppUserRepository

    @Mock
    private lateinit var searchAccountService: SearchAccountService

    @Mock
    private lateinit var operationBalanceService: OperationBalanceService

    @InjectMocks
    private lateinit var service: CreateTransactionService

    private lateinit var category: Category
    private lateinit var user: AppUser
    private lateinit var account: Account
    private lateinit var transaction: Transaction

    @BeforeEach
    fun setup() {
        category = Category(id = 1, name = "Alimentação", type = "EXPENSE")
        user = AppUser(10, "testUser", "test@test", "hash", Instant.now())
        account = Account(
            accountId = 1,
            accountName = "Conta Corrente",
            accountDescription = "Conta principal",
            accountInitialBalance = BigDecimal("1000.00"),
            accountCurrency = "BRL",
            userId = user
        )
        transaction = Transaction(
            id = 1,
            description = "Compra de supermercado",
            amount = BigDecimal("150.00"),
            type = TransactionType.EXPENSE,
            status = PaymentStatus.PENDING,
            categoryId = category,
            dueDate = LocalDate.of(2025, 8, 3),
            createdAt = Instant.now(),
            notes = "Compra do mês",
            recurrencePattern = null,
            installmentInfo = null,
            userId = user,
            accountId = account
        )
    }

    @Test
    @DisplayName("Deve criar uma transação única com sucesso")
    fun `should create single transaction successfully`() {
        val request = CreateTransactionRequest(
            description = "Compra de supermercado",
            amount = BigDecimal("150.00"),
            type = "EXPENSE",
            status = "PENDING",
            categoryId = 1,
            dueDate = LocalDate.of(2025, 8, 3),
            notes = "Compra do mês",
            recurrencePattern = null,
            totalInstallments = 0,
            userId = 10,
            accountId = 1
        )

        `when`(categoryRepository.findById(1)).thenReturn(Optional.of(category))
        `when`(appUserRepository.findById(10)).thenReturn(Optional.of(user))
        `when`(searchAccountService.searchAccountById(1)).thenReturn(account)
        `when`(transactionRepository.save(any(Transaction::class.java))).thenReturn(transaction)

        assertDoesNotThrow { service.execute(request) }

        verify(transactionRepository).save(any(Transaction::class.java))
        verify(operationBalanceService, never()).sumBalance(any(), any(), any())
        verify(operationBalanceService, never()).subtractBalance(any(), any(), any(),)
    }

    @Test
    @DisplayName("Deve criar transações recorrentes diárias com sucesso")
    fun `should create daily recurring transactions successfully`() {
        val request = CreateTransactionRequest(
            description = "Gasto diário",
            amount = BigDecimal("30.00"),
            type = "EXPENSE",
            status = "PENDING",
            categoryId = 1,
            dueDate = LocalDate.of(2025, 8, 1),
            notes = "Gasto diário",
            recurrencePattern = "DAILY",
            totalInstallments = 0,
            userId = 10,
            accountId = 1
        )

        `when`(categoryRepository.findById(1)).thenReturn(Optional.of(category))
        `when`(appUserRepository.findById(10)).thenReturn(Optional.of(user))
        `when`(searchAccountService.searchAccountById(1)).thenReturn(account)
        `when`(transactionRepository.saveAll(anyList())).thenReturn(listOf(transaction))

        assertDoesNotThrow { service.execute(request) }

        verify(transactionRepository).saveAll(anyList())
    }

    @Test
    @DisplayName("Deve criar transações parceladas mensais com sucesso")
    fun `should create monthly installment transactions successfully`() {
        val request = CreateTransactionRequest(
            description = "Compra parcelada",
            amount = BigDecimal("1200.00"),
            type = "EXPENSE",
            status = "PENDING",
            categoryId = 1,
            dueDate = LocalDate.of(2025, 8, 1),
            notes = "Compra em 12x",
            recurrencePattern = "MONTHLY",
            totalInstallments = 12,
            currentInstallment = 1,
            userId = 10,
            accountId = 1
        )

        `when`(categoryRepository.findById(1)).thenReturn(Optional.of(category))
        `when`(appUserRepository.findById(10)).thenReturn(Optional.of(user))
        `when`(searchAccountService.searchAccountById(1)).thenReturn(account)
        `when`(transactionRepository.saveAll(anyList())).thenReturn(listOf(transaction))

        assertDoesNotThrow { service.execute(request) }

        verify(transactionRepository).saveAll(anyList())
    }

    @Test
    @DisplayName("Deve falhar ao criar transação com valor zero")
    fun `should fail when creating transaction with zero amount`() {
        val request = CreateTransactionRequest(
            description = "Transação inválida",
            amount = BigDecimal.ZERO,
            type = "EXPENSE",
            categoryId = 1,
            dueDate = LocalDate.of(2025, 8, 3),
            totalInstallments = 0,
            userId = 10,
            accountId = 1
        )

        val exception = assertThrows<ValidationException> {
            service.execute(request)
        }
        assertEquals("Transaction amount must be greater than zero", exception.message)
    }

    @Test
    @DisplayName("Deve falhar ao criar transação com categoria inexistente")
    fun `should fail when creating transaction with non-existent category`() {
        val request = CreateTransactionRequest(
            description = "Transação inválida",
            amount = BigDecimal("100.00"),
            type = "EXPENSE",
            categoryId = 999,
            dueDate = LocalDate.of(2025, 8, 3),
            totalInstallments = 0,
            userId = 10,
            accountId = 1
        )

        `when`(categoryRepository.findById(999)).thenReturn(Optional.empty())

        val exception = assertThrows<IllegalArgumentException> {
            service.execute(request)
        }
        assertEquals("Category with id 999 not found", exception.message)
    }

    @Test
    @DisplayName("Deve falhar ao criar transação com usuário inexistente")
    fun `should fail when creating transaction with non-existent user`() {
        val request = CreateTransactionRequest(
            description = "Transação inválida",
            amount = BigDecimal("100.00"),
            type = "EXPENSE",
            categoryId = 1,
            dueDate = LocalDate.of(2025, 8, 3),
            totalInstallments = 0,
            userId = 999,
            accountId = 1
        )

        `when`(categoryRepository.findById(1)).thenReturn(Optional.of(category))
        `when`(appUserRepository.findById(999)).thenReturn(Optional.empty())

        val exception = assertThrows<IllegalArgumentException> {
            service.execute(request)
        }
        assertEquals("User with id 999 not found", exception.message)
    }

    @Test
    @DisplayName("Deve atualizar saldo quando transação está paga (INCOME)")
    fun `should update balance when transaction is paid income`() {
        val paidTransaction = transaction.copy(
            type = TransactionType.INCOME,
            status = PaymentStatus.PAID
        )

        `when`(operationBalanceService.sumBalance(1, BigDecimal("150.00"), any())).thenReturn(BigDecimal("1150.00"))

        assertDoesNotThrow { service.updateBalance(paidTransaction) }

        verify(operationBalanceService).sumBalance(eq(1), eq(BigDecimal("150.00")), any())
    }

    @Test
    @DisplayName("Deve atualizar saldo quando transação está paga (EXPENSE)")
    fun `should update balance when transaction is paid expense`() {
        val paidTransaction = transaction.copy(
            type = TransactionType.EXPENSE,
            status = PaymentStatus.PAID
        )

        `when`(operationBalanceService.subtractBalance(1, BigDecimal("150.00"), any(),)).thenReturn(BigDecimal("850.00"))

        assertDoesNotThrow { service.updateBalance(paidTransaction) }

        verify(operationBalanceService).subtractBalance(eq(1), eq(BigDecimal("150.00")), any(),)
    }

    @Test
    @DisplayName("Não deve atualizar saldo quando transação não está paga")
    fun `should not update balance when transaction is not paid`() {
        val pendingTransaction = transaction.copy(status = PaymentStatus.PENDING)

        assertDoesNotThrow { service.updateBalance(pendingTransaction) }

        verify(operationBalanceService, never()).sumBalance(any(), any(), any())
        verify(operationBalanceService, never()).subtractBalance(any(), any(), any(),)
    }
}
