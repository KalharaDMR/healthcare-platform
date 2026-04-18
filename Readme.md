# 🏥 Medicare Healthcare Platform

A cloud-native microservices-based healthcare management platform designed to streamline patient care, doctor management, appointment scheduling, telemedicine consultations, AI symptom checking, secure payments, and centralized administration.

The platform is built using **Spring Boot Microservices, React Frontend, PostgreSQL, Kubernetes, Docker, and API Gateway architecture.**

---

## 📌 Key Features

* Patient Registration & Login  
* Doctor Registration & Verification  
* Appointment Booking & Scheduling  
* Telemedicine Video Consultations  
* AI Symptom Checker (Gemini AI)  
* Secure Online Payments  
* Admin Dashboard & Management  
* Service Discovery using Eureka  
* API Gateway Routing  
* Kubernetes Deployment with Ingress  
* Dockerized Frontend & Backend Services  

---

## 🏗️ System Architecture

```text
                        ┌─────────────────────┐
                        │   React Frontend    │
                        │  (Dockerized UI)    │
                        └──────────┬──────────┘
                                   │
                                   ▼
                        ┌─────────────────────┐
                        │   API Gateway       │
                        │ Spring Cloud Gateway│
                        └──────────┬──────────┘
                                   │
        ┌──────────────────────────┼──────────────────────────┐
        ▼                          ▼                          ▼
┌──────────────┐          ┌──────────────┐           ┌──────────────┐
│ Auth Service │          │ Patient Svc  │           │ Doctor Svc   │
└──────────────┘          └──────────────┘           └──────────────┘

        ▼                          ▼                          ▼

┌──────────────┐          ┌──────────────┐           ┌──────────────┐
│ Appointment  │          │ Payment Svc  │           │ Telemedicine │
│ Service      │          │              │           │ Service      │
└──────────────┘          └──────────────┘           └──────────────┘

        ▼
┌──────────────┐
│ AI Symptom   │
│ Service      │
└──────────────┘

                ┌──────────────────────┐
                │ Eureka Server        │
                │ Service Discovery    │
                └──────────────────────┘

                ┌──────────────────────┐
                │ PostgreSQL Databases │
                └──────────────────────┘
```

---

## ⚙️ Technology Stack

### 🔹 Backend
* Java 21  
* Spring Boot  
* Spring Cloud Gateway  
* Spring Cloud Eureka  
* Spring Security  
* Spring Data JPA  
* Maven  

### 🔹 Frontend
* React.js  
* Axios  
* Bootstrap / CSS  
* Docker + Nginx  

### 🔹 Database
* PostgreSQL  

### 🔹 Deployment
* Docker  
* Docker Compose  
* Kubernetes (Minikube)  
* Ingress NGINX  
* Lens IDE  

### 🔹 AI / Integrations
* Gemini AI  
* Stripe Payment Gateway  
* Agora Video SDK  

---

## 📁 Project Structure

```bash
medicare-healthcare-platform/
│
├── authentication-service/
├── patient-service/
├── doctor-service/
├── appointment-service/
├── admin-service/
├── paymentService/
├── telemedicine-service/
├── aisymptom-service/
├── api-gateway/
├── eureka-server/
├── healthcare-frontend/
│
├── docker-compose.yml
├── k8s/
│   ├── deployment.yaml
│   └── ingress.yaml
│
└── README.md
```

---

## 🚀 Local Deployment Guide

### 1️⃣ Prerequisites

Install:

* Docker Desktop  
* Minikube  
* kubectl  
* Node.js (optional for frontend)  
* Lens IDE (optional)  

---

### 2️⃣ Start Kubernetes Cluster

```bash
minikube start
kubectl get nodes
```

---

### 3️⃣ Build Docker Images

```bash
docker build -t auth-service ./authentication-service
docker build -t patient-service ./patient-service
docker build -t doctor-service ./doctor-service
docker build -t appointment-service ./appointment-service
docker build -t admin-service ./admin-service
docker build -t payment-service ./paymentService
docker build -t telemedicine-service ./telemedicine-service
docker build -t aisymptom-service ./aisymptom-service
docker build -t api-gateway ./api-gateway
docker build -t eureka-server ./eureka-server
```

---

### 4️⃣ Deploy to Kubernetes

```bash
kubectl apply -f ./k8s/deployment.yaml
kubectl get pods
```