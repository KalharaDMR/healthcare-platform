import React, {
  createContext,
  useContext,
  useState,
  useCallback,
  useMemo,
  useEffect,
} from "react";
import { authApi } from "../api/authApi";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(localStorage.getItem("token"));
  const [loading, setLoading] = useState(true);

  // 🔥 FIX: Restore user from token on refresh
  useEffect(() => {
    const storedToken = localStorage.getItem("token");

    if (storedToken) {
      try {
        const payload = JSON.parse(atob(storedToken.split(".")[1]));
        const rawRoles = payload.roles || [];

        const roles = rawRoles.map((r) => r.replace(/^ROLE_/, ""));

        const userData = {
          username: payload.sub,
          roles,
        };

        setUser(userData);
        setToken(storedToken);
      } catch (err) {
        console.error("Invalid token");
        localStorage.removeItem("token");
        localStorage.removeItem("user");
      }
    }

    setLoading(false);
  }, []);

  const login = useCallback(async (username, password) => {
    setLoading(true);
    try {
      const res = await authApi.login({ username, password });
      const { token: jwt, username: uname } = res.data;

      const payload = JSON.parse(atob(jwt.split(".")[1]));
      const rawRoles = payload.roles || [];

      const roles = rawRoles.map((r) => r.replace(/^ROLE_/, ""));

      console.log("JWT payload:", payload);
      console.log("Extracted roles:", roles);

      const userData = { username: uname, roles };

      localStorage.setItem("token", jwt);
      localStorage.setItem("user", JSON.stringify(userData));

      setToken(jwt);
      setUser(userData);

      return { success: true, user: userData };
    } catch (err) {
      return {
        success: false,
        message: err.response?.data?.message || "Login failed",
      };
    } finally {
      setLoading(false);
    }
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem("token");
    localStorage.removeItem("user");
    setToken(null);
    setUser(null);
  }, []);

  const { isAdmin, isDoctor, isPatient } = useMemo(() => {
    const roles = user?.roles || [];
    return {
      isAdmin: roles.includes("ADMIN"),
      isDoctor: roles.includes("DOCTOR"),
      isPatient: roles.includes("PATIENT"),
    };
  }, [user]);

  const hasRole = useCallback(
    (role) => (user?.roles || []).includes(role.toUpperCase()),
    [user],
  );

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        loading,
        login,
        logout,
        hasRole,
        isAdmin,
        isDoctor,
        isPatient,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => useContext(AuthContext);
