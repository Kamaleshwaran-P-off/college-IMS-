-- Aptitude questions
INSERT INTO quiz_questions (category, question, option_a, option_b, option_c, option_d, correct_option) VALUES
('APTITUDE', 'If a train travels 60 km in 1.5 hours, what is its speed?', '30 km/h', '40 km/h', '45 km/h', '50 km/h', 'C'),
('APTITUDE', 'A shopkeeper marks an item at $120 and offers a 25% discount. What is the selling price?', '$90', '$95', '$100', '$105', 'A'),
('APTITUDE', 'If 5 workers complete a task in 12 days, how many days will 10 workers take?', '5', '6', '7', '8', 'B');

-- DSA questions
INSERT INTO quiz_questions (category, question, option_a, option_b, option_c, option_d, correct_option) VALUES
('DSA', 'What is the time complexity of binary search on a sorted array?', 'O(n)', 'O(log n)', 'O(n log n)', 'O(1)', 'B'),
('DSA', 'Which data structure is FIFO?', 'Stack', 'Queue', 'Tree', 'Graph', 'B'),
('DSA', 'In a max-heap, the smallest element is located at:', 'Root', 'Any leaf node', 'Middle level', 'Always at last index', 'B');
