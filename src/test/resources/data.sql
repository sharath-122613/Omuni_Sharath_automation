-- Insert test data for orders
INSERT INTO orders (order_number, customer_name, order_date, status, total_amount) VALUES
('ORD-001', 'John Doe', '2023-01-15 10:30:00', 'COMPLETED', 199.97),
('ORD-002', 'Jane Smith', '2023-01-16 14:45:00', 'PROCESSING', 89.98),
('ORD-003', 'Acme Corp', '2023-01-17 09:15:00', 'PENDING', 249.95);

-- Insert test data for order items
INSERT INTO order_items (order_id, product_id, product_name, quantity, unit_price, total_price) VALUES
(1, 'P1001', 'Laptop', 1, 1299.99, 1299.99),
(1, 'P1002', 'Wireless Mouse', 1, 29.99, 29.99),
(2, 'P1003', 'Keyboard', 2, 49.99, 99.98),
(3, 'P1004', 'Monitor', 1, 199.99, 199.99),
(3, 'P1005', 'Webcam', 1, 49.99, 49.99);
