import { useEffect, useState } from "react";
import AdminService from "../../services/adminService";
import AuthService from "../../services/auth.service";
import '../../assets/styles/user.css'
import Header from "../../components/utils/header";
import Loading from "../../components/utils/loading";
import Search from "../../components/utils/search";
import PageInfo from "../../components/utils/pageInfo";
import usePagination from "../../hooks/usePagination";
import Info from "../../components/utils/Info";
import Container from "../../components/utils/Container";
import toast, { Toaster } from "react-hot-toast";

function AdminTransactionsManagement() {

    const [data, setData] = useState([]);
    const [isFetching, setIsFetching] = useState(true);

    const {
        pageSize, pageNumber, noOfPages, searchKey,
        onNextClick, onPrevClick, setNoOfPages, setNoOfRecords, setSearchKey, getPageInfo
    } = usePagination()

    // Debug: Check user roles on mount
    useEffect(() => {
        const user = AuthService.getCurrentUser()
        if (user) {
            console.log("Current user:", user.email)
            console.log("User roles:", user.roles)
            if (!user.roles || !user.roles.includes("ROLE_ADMIN")) {
                console.warn("User does not have ROLE_ADMIN. Please log out and log back in.")
            }
        } else {
            console.error("No user found. Please log in.")
        }
    }, [])


    const getTransactions = async () => {
        setIsFetching(true)
        try {
            const response = await AdminService.getAllTransactions(pageNumber, pageSize, searchKey)
            if (response && response.data && response.data.status === 'SUCCESS') {
                setData(response.data.response?.data || [])
                setNoOfPages(response.data.response?.totalNoOfPages || 0)
                setNoOfRecords(response.data.response?.totalNoOfRecords || 0)
            } else {
                toast.error("Failed to fetch all transactions: Try again later!")
                setData([])
            }
        } catch (error) {
            console.error("Error fetching transactions:", error)
            if (error.response && error.response.status === 401) {
                toast.error("Authentication required. Please log out and log back in.")
            } else if (error.response && error.response.status === 403) {
                toast.error("You don't have permission to view transactions.")
            } else {
                toast.error("Failed to fetch all transactions: Try again later!")
            }
            setData([])
        } finally {
            setIsFetching(false)
        }
    }

    useEffect(() => {
        getTransactions();
    }, [searchKey, pageNumber])

    return (
        <Container activeNavId={4}>
            <Header title="Transactions" />
            <Toaster/>
            {(isFetching) && <Loading />}
            {(!isFetching) &&
                <>
                    <div className="utils page">
                        <Search onChange={(val) => setSearchKey(val)} placeholder="Search transactions" />
                        <PageInfo info={getPageInfo()} onPrevClick={onPrevClick} onNextClick={onNextClick}
                            pageNumber={pageNumber} noOfPages={noOfPages}
                        />
                    </div>
                    {(data.length === 0) && <Info text={"No transactions found!"} />}
                    {(data.length !== 0) && (
                        <table>
                            <TransactionsTableHeader />
                            <TransactionsTableBody data={data} />
                        </table>
                    )}
                </>
            }
        </Container>
    )
}

export default AdminTransactionsManagement;


function TransactionsTableHeader() {
    return (
        <tr>
            <th>Transaction Id</th> <th>Email</th>
            <th>Description</th> <th>Amount</th>
            <th>Category</th> <th>Date</th>
        </tr>
    )
}
function TransactionsTableBody({ data }) {
    return data.map((item) => {
        return (
            <tr key={item.transactionId}>
                <td>
                    <span>
                        {"T" + String(item.transactionId).padStart(5, '0')}
                    </span>
                </td>
                <td>{item.userEmail}</td>
                <td>{item.description || "-"}</td>
                <td>
                    {
                        item.transactionType === 1 ? "- " : "+ "
                    }
                    {item.amount}
                </td>
                <td>{item.categoryName}</td>
                <td>
                    {
                        new Date(item.date).toLocaleDateString('en-US', { year: 'numeric', month: 'long', day: '2-digit' })
                    }
                </td>
            </tr>
        )
    })
}