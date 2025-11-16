import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import AuthService from '../../services/auth.service';
import UserService from '../../services/userService';
import TransactionForm from '../../components/userTransactions/transactionForm';
import TransactionTypeSelectWrapper from '../../components/userTransactions/transactionTypeSelectWrapper';
import Header from '../../components/utils/header';
import Message from '../../components/utils/message';
import Loading from '../../components/utils/loading';
import useCategories from '../../hooks/useCategories';
import Info from '../../components/utils/Info';
import Container from '../../components/utils/Container';
import toast, { Toaster } from 'react-hot-toast';

const transactionTypes = [{ 'id': 1, 'name': 'Expense' }, { 'id': 2, 'name': 'Income' }]

function NewTransaction() {

    const [categories, isFetching] = useCategories();
    const [filteredCategories, setFilteredCategories] = useState([]);
    const [activeTransactionType, setTransactionType] = useState(1);
    const [isSaving, setIsSaving] = useState(false);

    const navigate = useNavigate();

    useEffect(() => {
        setFilteredCategories(categories.filter(cat => cat.transactionType.transactionTypeId === activeTransactionType));
    }, [categories, activeTransactionType])

    const onSubmit = async (data) => {
        setIsSaving(true)
        try {
            // Convert categoryId to integer and amount to number
            const categoryId = parseInt(data.category, 10)
            const amount = parseFloat(data.amount)
            
            if (isNaN(categoryId) || isNaN(amount)) {
                toast.error("Invalid category or amount!")
                setIsSaving(false)
                return
            }
            
            const response = await UserService.add_transaction(
                AuthService.getCurrentUser().email, categoryId, data.description, amount, data.date
            )
            if (response && response.data && response.data.status === "SUCCESS") {
                toast.success(response.data.response || "Transaction added successfully!")
                // Navigate to dashboard to see the updated data
                navigate("/user/dashboard", { state: { text: response.data.response } })
            } else {
                toast.error("Failed to add transaction: Try again later!")
            }
        } catch (error) {
            console.error("Error adding transaction:", error)
            console.error("Error response:", error.response)
            console.error("Error data:", error.response?.data)
            console.error("Request data:", {
                email: AuthService.getCurrentUser()?.email,
                categoryId: parseInt(data.category, 10),
                description: data.description,
                amount: parseFloat(data.amount),
                date: data.date
            })
            if (error.response && error.response.data) {
                if (error.response.data.response) {
                    toast.error(error.response.data.response)
                } else if (error.response.data.message) {
                    toast.error(error.response.data.message)
                } else {
                    toast.error(`Failed to add transaction: ${error.response.status} ${error.response.statusText}`)
                }
            } else if (error.message) {
                toast.error(`Failed to add transaction: ${error.message}`)
            } else {
                toast.error("Failed to add transaction: Try again later!")
            }
        } finally {
            setIsSaving(false)
        }
    }


    return (
        <Container activeNavId={2}>
            <Header title="New Transaction" />
            <Toaster/>
            {(isFetching) && <Loading />}
            {(!isFetching && categories.length === 0) && <Info text="No data found!" />}
            {
                (!isFetching && categories.length !== 0) && (
                    <>
                        <TransactionTypeSelectWrapper
                            transactionTypes={transactionTypes}
                            setTransactionType={setTransactionType}
                            activeTransactionType={activeTransactionType}
                        />
                        <TransactionForm categories={filteredCategories} onSubmit={onSubmit} isSaving={isSaving} />
                    </>
                )
            }
        </Container>
    )
}

export default NewTransaction;