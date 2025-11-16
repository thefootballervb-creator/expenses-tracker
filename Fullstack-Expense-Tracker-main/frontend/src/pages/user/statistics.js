import IncomeVsExpenseChart from "../../components/userDashboard/incomeVsExpenseChart";
import Header from "../../components/utils/header";
import Loading from '../../components/utils/loading';
import useExpenseVsIncomeSummary from '../../hooks/useExpenseVsIncomeSummary';
import Info from "../../components/utils/Info";
import Container from "../../components/utils/Container";
import toast, { Toaster } from "react-hot-toast";
import { useEffect, useRef, useMemo, useState } from "react";
import { useLocation } from "react-router-dom";

function UserStatistics() {
    const months = useMemo(() => getMonths(), [])
    const [refreshKey, setRefreshKey] = useState(0)
    const location = useLocation()
    const [data, isLoading, isError] = useExpenseVsIncomeSummary(months, refreshKey)
    const errorShownRef = useRef(false)

    // Refresh statistics when component mounts or when navigating to it
    useEffect(() => {
        setRefreshKey(prev => prev + 1)
    }, [location.pathname])

    // Show error toast only once when error occurs
    useEffect(() => {
        if (isError && !errorShownRef.current) {
            toast.error("Failed to fetch information. Try again later!")
            errorShownRef.current = true
        }
        if (!isError) {
            errorShownRef.current = false
        }
    }, [isError])

    return (
        <Container activeNavId={9}>
            <Header title="Statistics" />
            <Toaster/>
            {(isLoading) && <Loading />}
            {(!isLoading && isError) && <Info text="No data found!" />}
            {(!isLoading && !isError && data.length > 0) && <IncomeVsExpenseChart data={data} />}
            {(!isLoading && !isError && data.length === 0) && <Info text="No data found!" />}
        </Container>
    )
}

export default UserStatistics;

function getMonths() {
    const months = []
    const current_date = new Date()

    for (let i = 11; i >= 0; i--) {
        const date = new Date(current_date.getFullYear(), current_date.getMonth() - i, 1)
        months.push({
            id: date.getMonth() + 1,
            year: date.getFullYear(),
            monthName: date.toLocaleString('en-US', { month: 'long' })
        })
    }

    return months
}