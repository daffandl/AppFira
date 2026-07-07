package com.appfira.financeai.domain

import com.appfira.financeai.data.FinanceRepository
import com.appfira.financeai.logic.AIParser
import com.appfira.financeai.model.Transaction
import com.appfira.financeai.model.TransactionType

class ProcessChatUseCase(private val repository: FinanceRepository) {
    suspend operator fun invoke(input: String, forcedType: TransactionType?): Transaction? {
        val transaction = AIParser.parse(input, forcedType)
        if (transaction != null) {
            repository.insert(transaction)
        }
        return transaction
    }
}
