import { useEffect, useState } from 'react';
import UserService from '../../services/userService';
import AuthService from '../../services/auth.service';
import Header from '../../components/utils/header';
import Message from '../../components/utils/message';
import Loading from '../../components/utils/loading';
import Search from '../../components/utils/search';
import usePagination from '../../hooks/usePagination';
import PageInfo from '../../components/utils/pageInfo';
import TransactionList from '../../components/userTransactions/transactionList.js';
import { useLocation } from 'react-router-dom';
import Info from '../../components/utils/Info.js';
import Container from '../../components/utils/Container.js';
import toast, { Toaster } from 'react-hot-toast';


function Transactions() {

    const [userTransactions, setUserTransactions] = useState([]);
    const [isFetching, setIsFetching] = useState(true);
    const [transactionType, setTransactionType] = useState('')
    const location = useLocation();

    const {
        pageSize, pageNumber, noOfPages, sortField, sortDirec, searchKey,
        onNextClick, onPrevClick, setNoOfPages, setNoOfRecords, setSearchKey, getPageInfo
    } = usePagination('date')

    const getTransactions = async () => {
        setIsFetching(true)
        try {
            const response = await UserService.get_transactions(AuthService.getCurrentUser().email, pageNumber,
                pageSize, searchKey, sortField, sortDirec, transactionType)
            if (response && response.data && response.data.status === "SUCCESS") {
                setUserTransactions(response.data.response?.data || [])
                setNoOfPages(response.data.response?.totalNoOfPages || 0)
                setNoOfRecords(response.data.response?.totalNoOfRecords || 0)
            } else {
                toast.error("Failed to fetch all transactions: Try again later!")
            }
        } catch (error) {
            console.error("Error fetching transactions:", error)
            toast.error("Failed to fetch all transactions: Try again later!")
        } finally {
            setIsFetching(false)
        }
    }

    useEffect(() => {
        getTransactions()
    }, [pageNumber, searchKey, transactionType, sortDirec, sortField])

    useEffect(() => {
        location.state && toast.success(location.state.text)
        location.state = null
    }, [])

    return (
        <Container activeNavId={1}>
            <Header title="Transactions History" />
            <Toaster/>

            {(userTransactions.length === 0 && isFetching) && <Loading />}
            {(!isFetching) &&
                <>
                    <div className='utils'>
                        <Filter
                            setTransactionType={(val) => setTransactionType(val)}
                        />
                        <div className='page'>
                            <Search
                                onChange={(val) => setSearchKey(val)}
                                placeholder="Search transactions"
                            />
                            <PageInfo
                                info={getPageInfo()}
                                onPrevClick={onPrevClick}
                                onNextClick={onNextClick}
                                pageNumber={pageNumber}
                                noOfPages={noOfPages}
                            />
                        </div>
                    </div>
                    <div style={{ display: 'flex', justifyContent: 'center', gap: '12px', margin: '20px 0' }}>
                        <button
                            className='button outline'
                            onClick={async () => {
                                try {
                                    const user = AuthService.getCurrentUser();
                                    if (!user || !user.email) {
                                        toast.error('Please log out and log back in to download reports.');
                                        return;
                                    }
                                    if (!user.token) {
                                        toast.error('No authentication token found. Please log out and log back in.');
                                        return;
                                    }
                                    console.log('=== PDF DOWNLOAD DEBUG ===');
                                    console.log('User email:', user.email);
                                    console.log('Token exists:', !!user.token);
                                    console.log('Token preview:', user.token ? user.token.substring(0, 20) + '...' : 'NONE');
                                    const email = user.email;
                                    const response = await UserService.downloadTransactionsPdf(email);
                                    if (response && response.data) {
                                        const url = window.URL.createObjectURL(new Blob([response.data], { type: 'application/pdf' }));
                                        const link = document.createElement('a');
                                        link.href = url;
                                        link.setAttribute('download', 'transactions-report.pdf');
                                        document.body.appendChild(link);
                                        link.click();
                                        link.remove();
                                        window.URL.revokeObjectURL(url);
                                        toast.success('PDF report downloaded successfully!');
                                    } else {
                                        toast.error('No data received from server.');
                                    }
                                } catch (error) {
                                    console.error('Error downloading PDF report:', error);
                                    console.error('Error details:', {
                                        message: error.message,
                                        status: error.response?.status,
                                        statusText: error.response?.statusText,
                                        data: error.response?.data
                                    });
                                    // Handle blob error responses (401 errors come as JSON blobs)
                                    if (error.response && error.response.data instanceof Blob) {
                                        try {
                                            const text = await error.response.data.text();
                                            const errorData = JSON.parse(text);
                                            if (errorData.status === 401 || errorData.error === 'Unauthorized') {
                                                toast.error('Authentication failed. Please log out and log back in.');
                                                return;
                                            }
                                        } catch (e) {
                                            // If parsing fails, it might be a real PDF, but status code will tell us
                                        }
                                    }
                                    if (error.code === 'ERR_NETWORK' || error.message?.includes('Network Error')) {
                                        toast.error('Cannot connect to server. Please make sure the backend is running.');
                                    } else if (error.response && error.response.status === 401) {
                                        toast.error('Authentication failed. Please log out and log back in.');
                                    } else if (error.response && error.response.status === 403) {
                                        toast.error('You do not have permission to download reports.');
                                    } else if (error.message && error.message.includes('Authentication required')) {
                                        toast.error('Please log out and log back in to download reports.');
                                    } else {
                                        toast.error(`Failed to download PDF report: ${error.message || 'Unknown error'}`);
                                    }
                                }
                            }}
                        >
                            Download PDF
                        </button>
                        <button
                            className='button outline'
                            onClick={async () => {
                                try {
                                    const user = AuthService.getCurrentUser();
                                    if (!user || !user.email) {
                                        toast.error('Please log out and log back in to download reports.');
                                        return;
                                    }
                                    const email = user.email;
                                    const response = await UserService.downloadTransactionsExcel(email);
                                    if (response && response.data) {
                                        const url = window.URL.createObjectURL(new Blob([response.data], {
                                            type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
                                        }));
                                        const link = document.createElement('a');
                                        link.href = url;
                                        link.setAttribute('download', 'transactions-report.xlsx');
                                        document.body.appendChild(link);
                                        link.click();
                                        link.remove();
                                        window.URL.revokeObjectURL(url);
                                        toast.success('Excel report downloaded successfully!');
                                    } else {
                                        toast.error('No data received from server.');
                                    }
                                } catch (error) {
                                    console.error('Error downloading Excel report:', error);
                                    // Handle blob error responses (401 errors come as JSON blobs)
                                    if (error.response && error.response.data instanceof Blob) {
                                        try {
                                            const text = await error.response.data.text();
                                            const errorData = JSON.parse(text);
                                            if (errorData.status === 401 || errorData.error === 'Unauthorized') {
                                                toast.error('Authentication failed. Please log out and log back in.');
                                                return;
                                            }
                                        } catch (e) {
                                            // If parsing fails, it might be a real Excel file, but status code will tell us
                                        }
                                    }
                                    if (error.response && error.response.status === 401) {
                                        toast.error('Authentication failed. Please log out and log back in.');
                                    } else if (error.response && error.response.status === 403) {
                                        toast.error('You do not have permission to download reports.');
                                    } else if (error.message && error.message.includes('Authentication required')) {
                                        toast.error('Please log out and log back in to download reports.');
                                    } else {
                                        toast.error('Failed to download Excel report. Try again later!');
                                    }
                                }
                            }}
                        >
                            Download Excel
                        </button>
                    </div>
                    {(userTransactions.length === 0) && <Info text={"No transactions found!"} />}
                    {(userTransactions.length !== 0) && <TransactionList list={userTransactions} />}
                </>
            }
        </Container>
    )
}

export default Transactions;


function Filter({ setTransactionType }) {
    return (
        <select onChange={(e) => setTransactionType(e.target.value)} style={{ margin: '0 15px 0 0' }}>
            <option value="">All</option>
            <option value="expense">Expense</option>
            <option value="income">Income</option>
        </select>
    )
}


