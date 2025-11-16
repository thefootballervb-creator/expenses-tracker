package com.fullStack.expenseTracker.dataSeeders;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fullStack.expenseTracker.enums.ETransactionType;
import com.fullStack.expenseTracker.models.Category;
import com.fullStack.expenseTracker.models.TransactionType;
import com.fullStack.expenseTracker.repository.CategoryRepository;
import com.fullStack.expenseTracker.repository.TransactionTypeRepository;

@Component
public class CategoryDataSeeder {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TransactionTypeRepository transactionTypeRepository;

    private static final Map<ETransactionType, List<String>> DEFAULT_CATEGORIES = Map.of(
            ETransactionType.TYPE_EXPENSE, Arrays.asList(
                    "Housing", "Transportation", "Utilities", "Groceries", "Entertainment", 
                    "Healthcare", "Food", "Clothing", "Education", "Insurance", "Shopping", 
                    "Travel", "Bills", "Personal Care", "Gifts", "Donations", "Subscriptions",
                    "Restaurants", "Gas", "Parking", "Car Maintenance", "Public Transport",
                    "Phone", "Internet", "Electricity", "Water", "Rent", "Mortgage",
                    "Home Maintenance", "Furniture", "Electronics", "Books", "Movies",
                    "Music", "Sports", "Gym", "Hobbies", "Pet Care", "Childcare",
                    "Taxes", "Legal Fees", "Bank Fees", "ATM Fees", "Charity",
                    "Emergency Fund", "Savings", "Loan Payment", "Credit Card Payment"
            ),
            ETransactionType.TYPE_INCOME, Arrays.asList(
                    "Salary", "Bonus", "Investments", "Freelance", "Business", "Rental Income",
                    "Dividends", "Interest", "Commission", "Refund", "Other Income",
                    "Part-time Job", "Full-time Job", "Contract Work", "Consulting",
                    "Stock Trading", "Cryptocurrency", "Real Estate", "Royalties",
                    "Pension", "Social Security", "Unemployment", "Disability",
                    "Scholarship", "Grant", "Gift Money", "Inheritance", "Lottery",
                    "Cashback", "Rewards", "Side Hustle", "Online Business", "E-commerce"
            )
    );

    @EventListener
    @Transactional
    public void loadDefaultCategories(ContextRefreshedEvent event) {
        DEFAULT_CATEGORIES.forEach((transactionType, categories) -> {
            TransactionType existingTransactionType = transactionTypeRepository.findByTransactionTypeName(transactionType);
            TransactionType transactionTypeToUse = existingTransactionType != null
                    ? existingTransactionType
                    : transactionTypeRepository.save(new TransactionType(transactionType));

            final TransactionType finalTransactionType = transactionTypeToUse;

            categories.forEach(categoryName -> {
                if (!categoryRepository.existsByCategoryNameAndTransactionType(categoryName, finalTransactionType)) {
                    categoryRepository.save(new Category(categoryName, finalTransactionType, true));
                }
            });
        });
    }
}

