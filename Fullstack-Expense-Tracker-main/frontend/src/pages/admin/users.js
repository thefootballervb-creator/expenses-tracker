import { useEffect, useState } from "react";
import AdminService from "../../services/adminService"
import AuthService from "../../services/auth.service";
import Header from "../../components/utils/header";
import Loading from "../../components/utils/loading";
import usePagination from "../../hooks/usePagination";
import Search from "../../components/utils/search";
import PageInfo from "../../components/utils/pageInfo";
import Info from "../../components/utils/Info";
import Container from "../../components/utils/Container";
import toast, { Toaster } from "react-hot-toast";

function AdminUsersManagement() {

    const [data, setData] = useState([]);
    const [isFetching, setIsFetching] = useState(true);

    const {
        pageSize, pageNumber, noOfPages, searchKey,
        onNextClick, onPrevClick, setNoOfPages, setNoOfRecords, setSearchKey, getPageInfo
    } = usePagination()

    const getUsers = async () => {
        setIsFetching(true)
        try {
            console.log("Fetching users with params:", { pageNumber, pageSize, searchKey })
            const response = await AdminService.getAllUsers(pageNumber, pageSize, searchKey)
            console.log("Users API response:", response)
            if (response && response.data && response.data.status === 'SUCCESS') {
                const users = response.data.response?.data || []
                console.log("Fetched users:", users)
                setData(users)
                setNoOfPages(response.data.response?.totalNoOfPages || 0)
                setNoOfRecords(response.data.response?.totalNoOfRecords || 0)
            } else {
                console.error("Failed to fetch users - invalid response:", response)
                toast.error("Failed to fetch all users: Try again later!")
                setData([])
            }
        } catch (error) {
            console.error("Error fetching users:", error)
            console.error("Error details:", {
                message: error.message,
                response: error.response?.data,
                status: error.response?.status
            })
            if (error.response && error.response.status === 401) {
                const user = AuthService.getCurrentUser()
                if (user) {
                    console.warn("401 Error - User token may be expired or invalid")
                    console.warn("Current user roles in localStorage:", user.roles)
                    toast.error("Authentication failed. Your session may have expired. Please log out and log back in.", {
                        duration: 5000
                    })
                } else {
                    toast.error("Please log in to access this page.")
                }
            } else if (error.response && error.response.status === 403) {
                toast.error("You don't have permission to view users. Admin role required.")
            } else {
                toast.error("Failed to fetch all users: Try again later!")
            }
            setData([])
        } finally {
            setIsFetching(false)
        }
    }

    const disableOrEnable = async (userId) => {
        try {
            const response = await AdminService.disableOrEnableUser(userId)
            if (response && response.data && response.data.status === 'SUCCESS') {
                toast.success(response.data.response || "User status updated successfully!")
                getUsers() // Refresh the user list instead of reloading the page
            } else {
                toast.error("Failed to update user: Try again later!")
            }
        } catch (error) {
            console.error("Error updating user:", error)
            if (error.response && error.response.data && error.response.data.response) {
                toast.error(error.response.data.response)
            } else {
                toast.error("Failed to update user: Try again later!")
            }
        }
    }

    // Debug: Check user roles on mount
    useEffect(() => {
        const user = AuthService.getCurrentUser()
        if (user) {
            console.log("AdminUsersManagement - Current user:", user.email)
            console.log("AdminUsersManagement - User roles:", user.roles)
            if (!user.roles || !user.roles.includes("ROLE_ADMIN")) {
                console.warn("AdminUsersManagement - User does not have ROLE_ADMIN!")
                console.warn("AdminUsersManagement - Please log out and log back in to refresh your session.")
            }
        } else {
            console.error("AdminUsersManagement - No user found. Please log in.")
        }
    }, [])

    useEffect(() => {
        getUsers();
    }, [searchKey, pageNumber])

    return (
        <Container activeNavId={5}>
            <Header title="Users" />
            <Toaster/>

            {(isFetching) && <Loading />}
            {(!isFetching) &&
                <>
                    <div className="utils page">
                        <Search onChange={(val) => setSearchKey(val)} placeholder="Search users" />
                        <PageInfo info={getPageInfo()} onPrevClick={onPrevClick} onNextClick={onNextClick}
                            pageNumber={pageNumber} noOfPages={noOfPages}
                        />
                    </div>
                    {(data.length === 0) && <Info text={"No users found!"} />}
                    {(data.length !== 0) && (
                        <table>
                            <UsersTableHeader />
                            <UsersTableBody data={data} disableOrEnable={disableOrEnable} />
                        </table>
                    )}
                </>
            }
        </Container>
    )
}

export default AdminUsersManagement;


function UsersTableHeader() {
    return (
        <tr>
            <th>User Id</th> <th>Username</th> <th>Email</th>
            <th>Tot. Expense(Rs.)</th> <th>Tot. Income(Rs.)</th>
            <th>Tot. No. Transactions</th> <th>Status</th> <th>Action</th>
        </tr>
    )
}

function UsersTableBody({ data, disableOrEnable }) {
    return (
        data.map((item) => {
            return (
                <tr key={item.id}>
                    <td>{"U" + String(item.id).padStart(5, '0')}</td>
                    <td>{item.username}</td>
                    <td>{item.email}</td>
                    <td>Rs. {item.expense || 0.0}</td>
                    <td>Rs. {item.income || 0.0}</td>
                    <td>{item.noOfTransactions || 0}</td>
                    {
                        item.enabled ? <td style={{ color: '#6aa412' }}>Enabled</td> : <td style={{ color: '#ff0000' }}>Disabled</td>
                    }

                    <td>
                        {
                            (item.enabled) ?
                                <button
                                    onClick={() => disableOrEnable(item.id)}
                                    style={{ backgroundColor: '#ff0000' }}
                                >Disable
                                </button> :
                                <button
                                    onClick={() => disableOrEnable(item.id)}
                                    style={{ backgroundColor: '#6aa412' }}
                                >Enable
                                </button>
                        }
                    </td>
                </tr>
            )
        })
    )
}