import '../../assets/styles/dashboard.css';
import { useState, useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import DashboardDetailBox from '../../components/userDashboard/dashboardDetailBox';
import CategoryExpenseChart from '../../components/userDashboard/categoryExpenseChart';
import Header from '../../components/utils/header';
import Budget from '../../components/userDashboard/budget';
import useDashboard from '../../hooks/useDashboard';
import Loading from '../../components/utils/loading';
import Info from '../../components/utils/Info';
import Container from '../../components/utils/Container';
import toast, { Toaster } from 'react-hot-toast';

function Dashboard() {

    const months = getMonths()
    const [currentMonth, setMonth] = useState(months[0])
    const [refreshKey, setRefreshKey] = useState(0)
    const location = useLocation()

    const [total_expense, total_income, cash_in_hand, no_of_transactions, categorySummary, budgetAmount,
        saveBudget, isLoading, isError] = useDashboard(currentMonth, refreshKey)

    // Refresh dashboard when component mounts or when navigating to it
    useEffect(() => {
        // Refresh when component mounts or when location changes (user navigates to dashboard)
        setRefreshKey(prev => prev + 1)
    }, [location.pathname])

    // Show error toast when there's an error
    useEffect(() => {
        if (isError) {
            toast.error("Failed to fetch information. Try again later!")
        }
    }, [isError])

    const onMonthChange = (id) => {
        const month = months.find(m => m.id === Number(id))
        setMonth(month)
    }

    return (
        <Container activeNavId={0}>
            <Header title="Dashboard" />
            <Toaster/>
            {(isLoading) && <Loading />}
            {(!isError) && <SelectMonth months={months} onMonthChange={onMonthChange} currentMonth={currentMonth} />}
            {(!isLoading && !isError && total_expense === 0 && total_income === 0) && (
                <Info text={`You have no transactions in ${currentMonth.monthName} ${currentMonth.year}! Make sure the transaction date matches the selected month.`} />
            )}
            {
                (!isError && (total_expense !== 0 || total_income !== 0)) && <>
                    <DashboardDetailBox total_expense={total_expense} total_income={total_income} cash_in_hand={cash_in_hand} no_of_transactions={no_of_transactions} />
                    <div className='dashboard-chart'>
                        <CategoryExpenseChart categorySummary={categorySummary} />
                        <Budget totalExpense={total_expense} budgetAmount={budgetAmount} saveBudget={saveBudget} currentMonth={currentMonth} />
                    </div>
                </>
            }
        </Container>

    )
}

export default Dashboard;

function getMonths() {
    const months = []
    const current_date = new Date()

    for (let i = 0; i <= 11; i++) {
        const date = new Date(current_date.getFullYear(), current_date.getMonth() - i, 1)
        months.push({
            id: date.getMonth() + 1,
            year: date.getFullYear(),
            monthName: date.toLocaleString('en-US', { month: 'long' })
        })
    }

    return months;
}

function SelectMonth({ months, onMonthChange, currentMonth }) {
    return (
        <div>
            <select 
                value={currentMonth ? `${currentMonth.id}` : ''} 
                onChange={(e) => onMonthChange(e.target.value)}
            >
                {
                    months.map((m) => {
                        return (
                            <option value={m.id} key={m.id}>{m.monthName} {m.year}</option>
                        )
                    })
                }
            </select>
        </div>
    )
}