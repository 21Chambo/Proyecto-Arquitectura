module.exports = {
  '/api': {
    target: `http://localhost:${process.env.API_PORT || 9090}`
  }
};
