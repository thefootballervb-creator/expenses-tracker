import { useEffect, useState } from "react";
import useCategories from "./useCategories";
import UserService from "../services/userService";
import AuthService from "../services/auth.service";

function useDashboard(currentMonth, refreshKey = 0) {
    const [total_income, setIncome] = useState(0)
    const [total_expense, setExpense] = useState(0)
    const [no_of_transactions, setTransactions] = useState(0)
    const cash_in_hand = total_income > total_expense ? Number((total_income - total_expense)?.toFixed(2)) : 0;
    const [categories] = useCategories()
    const [categorySummary, setCategorySummary] = useState([])
    const [budgetAmount, setBudgetAmount] = useState(0)
    const [isLoading, setIsLoading] = useState(true);
    const [isError, setIsError] = useState(false);


    const generateTransactionSummary = async () => {
        setIsLoading(true)
        setIsError(false)
        try {
            const user = AuthService.getCurrentUser()
            if (!user || !user.id) {
                console.error("User not found or not authenticated")
                setIsError(true)
                setIsLoading(false)
                return
            }

            const income_response = await UserService.getTotalIncomeOrExpense(user.id, 2, currentMonth.id, currentMonth.year)
            if (income_response && income_response.data && income_response.data.status === "SUCCESS") {
                setIncome(Number((income_response.data.response) ? income_response.data.response.toFixed(2) : 0))
            } else {
                setIncome(0)
            }

            const expense_response = await UserService.getTotalIncomeOrExpense(user.id, 1, currentMonth.id, currentMonth.year)
            if (expense_response && expense_response.data && expense_response.data.status === "SUCCESS") {
                setExpense(Number((expense_response.data.response) ? expense_response.data.response.toFixed(2) : 0))
            } else {
                setExpense(0)
            }

            const no_response = await UserService.getTotalNoOfTransactions(user.id, currentMonth.id, currentMonth.year)
            if (no_response && no_response.data && no_response.data.status === "SUCCESS") {
                setTransactions(no_response.data.response || 0)
            } else {
                setTransactions(0)
            }
        } catch (error) {
            console.error("Error fetching transaction summary:", error)
            console.error("Error details:", {
                message: error.message,
                response: error.response?.data,
                month: currentMonth.id,
                year: currentMonth.year
            })
            setIsError(true)
            setIncome(0)
            setExpense(0)
            setTransactions(0)
        } finally {
            setIsLoading(false)
        }
    }

    const generateCategorySummary = async () => {
        if (!categories || categories.length === 0) return
        setIsLoading(true)
        const filtered = [];
        try {
            await Promise.all(categories.filter(cat => cat.transactionType.transactionTypeId === 1).map(async (cat) => {
                try {
                    const response = await UserService.getTotalByCategory(AuthService.getCurrentUser().email, cat.categoryId, currentMonth.id, currentMonth.year);
                    if (response.data.status === "SUCCESS" && response.data.response) {
                        filtered.push({ name: cat.categoryName, amount: Number(response.data.response ? response.data.response.toFixed(2) : 0) });
                    }
                } catch (error) {
                    console.error(`Error fetching category ${cat.categoryName}:`, error)
                }
            }));
            setCategorySummary(filtered)
        } catch (error) {
            console.error("Error generating category summary:", error)
            setIsError(true)
        } finally {
            setIsLoading(false)
        }
    }

    const fetchBudget = async () => {
        setIsLoading(true)
        try {
            const response = await UserService.getBudget(currentMonth.id, currentMonth.year)
            setBudgetAmount(response.data.response || 0)
        } catch (error) {
            console.error("Error fetching budget:", error)
            setBudgetAmount(0)
        } finally {
            setIsLoading(false)
        }
    }

    const saveBudget = async (d) => {
        const response = await UserService.createBudget(d.amount)
            .then((response) => {
            })
            .catch((error) => {
                setIsError(true)
            })
        fetchBudget()
    }

    useEffect(() => {
        generateTransactionSummary()
        if (categories) {
            generateCategorySummary()
        }
        fetchBudget()
    }, [currentMonth, categories, refreshKey])

    return [
        total_expense,
        total_income,
        cash_in_hand,
        no_of_transactions,
        categorySummary,
        budgetAmount,
        saveBudget,
        isLoading,
        isError
    ]


}

export default useDashboard;