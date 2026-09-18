import unittest, json, os, sys
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from spider import create_spider

class TestPySpider(unittest.TestCase):
    def test_home(self):
        s = create_spider()
        data = json.loads(s.home_content())
        self.assertIn("class", data)
        self.assertGreaterEqual(len(data["class"]), 1)

    def test_category(self):
        s = create_spider()
        data = json.loads(s.category_content())
        self.assertIn("list", data)

    def test_search(self):
        s = create_spider()
        data = json.loads(s.search_content("测试"))
        self.assertIn("list", data)
        self.assertEqual(data["list"][0]["name"], "测试")

if __name__ == "__main__":
    unittest.main()
