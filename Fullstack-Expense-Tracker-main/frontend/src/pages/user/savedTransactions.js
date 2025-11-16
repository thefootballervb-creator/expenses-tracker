import { useEffect, useState } from 'react';
import UserService from '../../services/userService';
import Header from '../../components/utils/header';
import Loading from '../../components/utils/loading';
import { Link, useLocation } from 'react-router-dom';
import Info from '../../components/utils/Info.js';
import Container from '../../components/utils/Container.js';
import toast, { Toaster } from 'react-hot-toast';

function SavedTransactions() {
    const [userTransactions, setUserTransactions] = useState([]);
    const [isLoading, setIsLoading] = useState(false);
    const location = useLocation();

    const getTransactions = async () => {
        setIsLoading(true)
        try {
            const response = await UserService.getSavedTransactions()
            if (response && response.data && response.data.status === "SUCCESS") {
                console.log(response.data)
                setUserTransactions(response.data.response || [])
            } else {
                toast.error("Failed to fetch all transactions: Try again later!")
            }
        } catch (error) {
            console.error("Error fetching saved transactions:", error)
            toast.error("Failed to fetch all transactions: Try again later!")
        } finally {
            setIsLoading(false)
        }
    }

    const saveTransaction = async (id) => {
        setIsLoading(true)
        try {
            const response = await UserService.addSavedTransaction(id)
            if (response && response.data && response.data.response) {
                toast.success(response.data.response)
                getTransactions()
            } else {
                toast.error("Failed to save transaction: Try again later!")
            }
        } catch (error) {
            console.error("Error saving transaction:", error)
            toast.error("Failed to save transaction: Try again later!")
        } finally {
            setIsLoading(false)
        }
    }

    const skipTransaction = async (id) => {
        setIsLoading(true)
        try {
            const response = await UserService.skipSavedTransaction(id)
            if (response && response.data && response.data.response) {
                toast.success(response.data.response)
                getTransactions()
            } else {
                toast.error("Failed to skip transaction: Try again later!")
            }
        } catch (error) {
            console.error("Error skipping transaction:", error)
            toast.error("Failed to skip transaction: Try again later!")
        } finally {
            setIsLoading(false)
        }
    }

    useEffect(() => {
        getTransactions()
    }, [])

    useEffect(() => {
        location.state && toast.success(location.state.text)
        location.state = null
    }, [])

    return (
        <Container activeNavId={11}>
            <Header title="Saved Transactions" />
            <Toaster />

            {(isLoading) && <Loading />}
            {(!isLoading) &&
                <>
                    <div className='st-top'>
                        <h2>Save your recurring transactions to quick access!</h2>
                        <Link to={ `/user/savedTransactions/new`}><button>+ Add new</button></Link>
                    </div>
                    
                    {(userTransactions.length === 0) && <Info text={"No transactions found!"} />}
                    {(userTransactions.length !== 0) && <SavedTransactionList list={userTransactions} saveTransaction={saveTransaction} skipTransaction={skipTransaction}/>}
                </>
            }
        </Container>
    )
}

export default SavedTransactions;

function SavedTransactionList({ list, saveTransaction, skipTransaction }) {
    return (
        <div className='st-list'>
         { list.map(t => {
            return (
                t.dueInformation && <div className='st-card' key={t.planId}>
                <div className='topic'>
                    <h4>{t.categoryName}</h4>
                    <h4>{ t.transactionType === 1 ? "- " : "+ " } Rs. {t.amount}</h4>
                </div>
                <p>
                    {t.description}
                </p>
                <p className={t.dueInformation.includes('overdue') ? 'due overdue' : t.dueInformation.includes('Today') ? 'due today' : 'due'}>
                    {t.dueInformation} ({t.frequency}) 
                    {t.dueInformation.includes('overdue') ? 
                        <i className='fa fa-exclamation-circle' aria-hidden='true'></i> : 
                        t.dueInformation.includes('Today') ? 
                        <i className='fa fa-history' aria-hidden='true'></i> : <></>}
                    
                </p>
                <div>
                    <button 
                        onClick={() => saveTransaction(t.planId)}
                    >Confirm</button>
                    <button 
                        className='button outline'
                        onClick={() => skipTransaction(t.planId)}
                    >Skip</button>
                    <Link to={`/user/editSavedTransaction/${t.planId}`}><button className='button outline'>Edit</button></Link>
                </div>
            </div>
                
            )
        }) }
        </div>
    )
}