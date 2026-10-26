# Diffie–Hellman Key Exchange Simulator (CNS Mini Project)

Stack: Java 21, Spring Boot 3.5.x, React + Vite, MySQL 8.

## Features
- Textbook Diffie–Hellman simulation using modular exponentiation (`BigInteger.modPow`).
- Validates educational parameters and caps `p` at 31 bits to prevent expensive requests.
- Generates private exponents with `SecureRandom` when omitted.
- MITM demonstration using two independent exchanges.
- MySQL history stores public values and match status only; it does not store private exponents or shared secrets.
- React UI for normal exchange, MITM demonstration, and history.

> Educational warning: The small-parameter simulator is not suitable for real-world security. Diffie–Hellman alone does not authenticate participants, so it is vulnerable to MITM. Real systems use authenticated protocols and vetted cryptographic libraries. Never use this demo to protect real data.

## Requirements
- JDK 21
- Maven 3.9+
- Node.js 20+
- MySQL 8+

## 1. Create database
```sql
CREATE DATABASE dh_simulator;
```

## 2. Start backend
Edit `backend/src/main/resources/application.properties` with your MySQL username/password, or set environment variables:
- `DB_URL` (default `jdbc:mysql://localhost:3306/dh_simulator`)
- `DB_USERNAME` (default `root`)
- `DB_PASSWORD` (default empty)

Then:
```bash
cd backend
mvn spring-boot:run
```
API runs at `http://localhost:8080`.

## 3. Start frontend
In another terminal:
```bash
cd frontend
npm install
npm run dev
```
Open the Vite URL printed in the terminal (usually `http://localhost:5173`).

## API endpoints
- `GET /api/health` — health check
- `POST /api/simulations/diffie-hellman` — run normal exchange
- `POST /api/simulations/mitm` — simulate an unauthenticated MITM
- `GET /api/simulations/history` — latest 50 saved public-only records

### Normal exchange request
```json
{
  "p": 23,
  "g": 5,
  "alicePrivate": 6,
  "bobPrivate": 15
}
```
`alicePrivate` and `bobPrivate` are optional. If omitted, the backend generates them randomly. Use small values only for classroom demonstrations.

### MITM request
```json
{
  "p": 23,
  "g": 5,
  "alicePrivate": 6,
  "bobPrivate": 15,
  "attackerPrivateForAlice": 7,
  "attackerPrivateForBob": 11
}
```
Attacker exponents are optional and generated randomly if omitted.

## Example expected result
With `p=23`, `g=5`, `a=6`, `b=15`:
- Alice public value: `8`
- Bob public value: `19`
- Alice shared value: `2`
- Bob shared value: `2`
- `secretsMatch`: `true`

## Safe testing checklist
1. Test `p=23`, `g=5`, `a=6`, `b=15`; verify both shared values are `2`.
2. Omit private exponents and verify the backend generates them.
3. Try `p=1`, `g=1`, negative exponents, `g >= p`, or `p > 2^31-1`; expect HTTP 400.
4. Run the MITM demo and verify Alice and Bob derive different secrets, while the attacker derives each corresponding secret.
5. Restart the backend and inspect history: public values are saved; private exponents and shared secrets are not.
6. Try opening the frontend with the backend stopped; the UI should show an API error rather than pretending the request succeeded.

## Academic concepts
- Confidentiality: DH establishes key material but does not itself encrypt messages.
- Key agreement: both participants derive the same value without sending it directly.
- Discrete logarithm problem: recovering a private exponent from public parameters is computationally hard when secure parameters are used.
- Authentication: basic DH does not authenticate the other party.
- MITM: an active intermediary can establish one secret with Alice and another with Bob.
- Production systems: use TLS or vetted protocols and libraries; do not design your own production cryptographic protocol.
