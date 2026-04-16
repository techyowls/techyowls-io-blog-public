import express, { Express } from 'express';
import cors from 'cors';
import helmet from 'helmet';
import { todoRouter } from './routes/todos';

const app: Express = express();
const PORT = process.env.PORT || 3000;

// Middleware
app.use(helmet());
app.use(cors());
app.use(express.json());

// Routes
app.use('/api/todos', todoRouter);

app.get('/health', (req, res) => {
  res.json({ status: 'ok', timestamp: new Date().toISOString() });
});

app.listen(PORT, () => {
  console.log(`✨ Todo API running on port ${PORT}`);
});

export default app;
