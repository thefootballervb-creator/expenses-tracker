import { useNavigate } from "react-router-dom";
import Logo from "../../components/utils/Logo";
import AuthService from "../../services/auth.service";

function UnAuthorizedAccessPage() {

    const navigate = useNavigate()
    const user = AuthService.getCurrentUser()
    
    const handleLogout = () => {
        AuthService.logout_req()
        navigate("/auth/login")
        window.location.reload()
    }
    
    return (
        <main style={{ textAlign: 'center', padding: '50px 20px' }}>
            <Logo/>
            <h1 style={{ color: '#1976d2', marginTop: '30px' }}>401 - Unauthorized!</h1>
            <h3 style={{ color: '#666', maxWidth: '600px', margin: '20px auto', lineHeight: '1.6' }}>
                Sorry, it looks like you don't have permission to access this page. 
                {user ? (
                    <>
                        <br/><br/>
                        Your current session may have expired or your account doesn't have the required permissions.
                        <br/><br/>
                        <strong>Please log out and log back in to refresh your session.</strong>
                    </>
                ) : (
                    <>
                        <br/><br/>
                        Please log in to access this page.
                    </>
                )}
            </h3>
            <div style={{ marginTop: '30px', display: 'flex', gap: '15px', justifyContent: 'center', flexWrap: 'wrap' }}>
                {user ? (
                    <>
                        <button
                            onClick={handleLogout}
                            style={{
                                padding: '12px 24px',
                                backgroundColor: '#1976d2',
                                color: 'white',
                                border: 'none',
                                borderRadius: '5px',
                                cursor: 'pointer',
                                fontSize: '16px',
                                fontWeight: 'bold'
                            }}
                        >Log Out & Login Again</button>
                        <button
                            onClick={() => navigate("/")}
                            style={{
                                padding: '12px 24px',
                                backgroundColor: '#f5f5f5',
                                color: '#333',
                                border: '1px solid #ddd',
                                borderRadius: '5px',
                                cursor: 'pointer',
                                fontSize: '16px'
                            }}
                        >Go to Home</button>
                    </>
                ) : (
                    <button
                        onClick={() => navigate("/auth/login")}
                        style={{
                            padding: '12px 24px',
                            backgroundColor: '#1976d2',
                            color: 'white',
                            border: 'none',
                            borderRadius: '5px',
                            cursor: 'pointer',
                            fontSize: '16px',
                            fontWeight: 'bold'
                        }}
                    >Go to Login</button>
                )}
            </div>
        </main>
    )
}
export default UnAuthorizedAccessPage;