INSERT INTO invoices VALUES (${body[number]}, ${body[value]}) RETURNING *;
